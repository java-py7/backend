import os
import subprocess
import time
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# 1. Cloud-standard DATABASE_URL (Render, Supabase, Neon, Railway, Heroku)
raw_database_url = os.getenv("DATABASE_URL")

if raw_database_url:
    # SQLAlchemy 2.0 requires postgresql:// instead of legacy postgres://
    if raw_database_url.startswith("postgres://"):
        DATABASE_URL = raw_database_url.replace("postgres://", "postgresql://", 1)
    else:
        DATABASE_URL = raw_database_url
    IS_CLOUD_DB = True
else:
    IS_CLOUD_DB = False
    PG_PORT = os.getenv("DB_PORT", "5433")
    PG_HOST = os.getenv("DB_HOST", "127.0.0.1")
    PG_USER = os.getenv("DB_USER", "postgres")
    PG_PASSWORD = os.getenv("DB_PASSWORD", "")
    PG_DBNAME = os.getenv("DB_NAME", "vendorcoinpay")

    if PG_PASSWORD:
        DATABASE_URL = f"postgresql://{PG_USER}:{PG_PASSWORD}@{PG_HOST}:{PG_PORT}/{PG_DBNAME}"
    else:
        DATABASE_URL = f"postgresql://{PG_USER}@{PG_HOST}:{PG_PORT}/{PG_DBNAME}"

def ensure_postgres_running():
    """Ensure dedicated PostgreSQL instance is running (Local Windows development only)."""
    if IS_CLOUD_DB:
        return

    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASSWORD, dbname="postgres", connect_timeout=2)
        conn.close()
        return
    except Exception:
        pass

    if os.name == 'nt':
        datadir = r"C:\Users\ADMIN\vcp_pgdata"
        pg_ctl = r"C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe"
        if os.path.exists(datadir) and os.path.exists(pg_ctl):
            pid_file = os.path.join(datadir, "postmaster.pid")
            if os.path.exists(pid_file):
                try:
                    os.remove(pid_file)
                except Exception:
                    pass
            subprocess.Popen([pg_ctl, "-D", datadir, "-o", f"-p {PG_PORT}", "start"], creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
            for _ in range(15):
                time.sleep(0.5)
                try:
                    conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASSWORD, dbname="postgres", connect_timeout=1)
                    conn.close()
                    break
                except Exception:
                    continue

def ensure_database_exists():
    """Create the vendorcoinpay database if it doesn't already exist (Local development only)."""
    if IS_CLOUD_DB:
        return

    try:
        conn = psycopg2.connect(host=PG_HOST, port=PG_PORT, user=PG_USER, password=PG_PASSWORD, dbname="postgres")
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cur = conn.cursor()
        cur.execute(f"SELECT 1 FROM pg_catalog.pg_database WHERE datname = '{PG_DBNAME}'")
        exists = cur.fetchone()
        if not exists:
            cur.execute(f"CREATE DATABASE {PG_DBNAME}")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"[Database] Notice when checking/creating database: {e}")

# Only run local helpers when not in cloud
if not IS_CLOUD_DB:
    ensure_postgres_running()
    ensure_database_exists()

# Engine creation with pre-ping and connection pooling
engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    pool_size=10,
    max_overflow=20,
    pool_recycle=300
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def init_db():
    """Create all tables in the database."""
    import app.models.user
    import app.models.wallet
    import app.models.transaction
    Base.metadata.create_all(bind=engine)
