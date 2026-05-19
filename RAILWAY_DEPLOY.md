# Deploying Pharmacie to Railway

This is the **runbook** to take the app from your laptop to a live HTTPS URL
on the public internet. Total time: ~30–45 min, ~$5/month.

Architecture on Railway (one project, three services):

```
   Internet ──HTTPS──▶  frontend (nginx + Angular)  ──HTTP──▶  backend (Spring Boot)  ──TCP──▶  Postgres (managed)
                          public domain                          internal only                    internal only
```

The frontend is the only service exposed to the internet. Nginx proxies
`/api/**` to the backend's public Railway URL, and the backend talks to the
managed Postgres over Railway's private network.

---

## 0. One-time prerequisites (5 min)

1. Make sure the latest `main` is pushed to GitHub: `git push origin main`.
2. Open [railway.com](https://railway.com) and click **Login** → **Login with
   GitHub**. Use your personal GitHub account (`alexandrastefaniadan`).
3. Railway will offer a free trial (≈$5 of free usage). After that you upgrade
   to the **Hobby plan ($5/month)** which is what we want.

## 1. Create the project and the database (3 min)

1. From the Railway dashboard click **New Project** → **Empty Project**.
2. Name it `pharmacie`.
3. Inside the project click **+ Create** → **Database** → **Add PostgreSQL**.
   Railway provisions a Postgres 16 instance and injects the standard
   `PGHOST` / `PGPORT` / `PGDATABASE` / `PGUSER` / `PGPASSWORD` variables
   into every other service in the same project automatically.

## 2. Deploy the backend (5 min)

1. **+ Create** → **GitHub Repo** → pick `alexandrastefaniadan/pharmacie`.
   The first time, Railway will ask permission to access the repo — accept.
2. After it's created, click the new service and open the **Settings** tab:
   - **Service name**: `backend`
   - **Source** → **Root Directory**: `backend`
     (so Railway uses `backend/Dockerfile` and `backend/railway.json`).
   - **Networking** → **Public Networking** → click **Generate Domain**.
     Railway gives you something like `pharmacie-backend-production.up.railway.app`.
     Copy that URL — you'll paste it into the frontend in step 3.
3. Open the **Variables** tab and add:

   | Variable               | Value                                                 |
   |------------------------|-------------------------------------------------------|
   | `SPRING_PROFILES_ACTIVE` | `prod`                                              |
   | `ADMIN_USERNAME`       | `admin`                                               |
   | `ADMIN_PASSWORD`       | a long random string (use `openssl rand -base64 24`)  |
   | `CORS_ALLOWED_ORIGINS` | leave blank for now — we'll fill it in step 3         |

   You don't need to add `PGHOST`, `PGPORT`, etc. — Railway injects those
   automatically because the Postgres plugin is in the same project.
4. Click **Deploy**. Watch the **Deployments** tab; the first build takes
   ~4 minutes (Gradle download + bootJar). When the Healthcheck on
   `/actuator/health` goes green you're done.

## 3. Deploy the frontend (5 min)

1. Back at the project, click **+ Create** → **GitHub Repo** → same repo.
2. In **Settings**:
   - **Service name**: `frontend`
   - **Source** → **Root Directory**: `frontend`
   - **Networking** → **Public Networking** → **Generate Domain**.
     Copy this domain — this is the URL you'll open in the browser at the
     pharmacy (e.g. `pharmacie-frontend-production.up.railway.app`).
3. Open **Variables** and add:

   | Variable      | Value                                                          |
   |---------------|----------------------------------------------------------------|
   | `BACKEND_URL` | `https://<paste the backend domain from step 2>`               |
   | `PORT`        | leave **unset** — Railway sets this automatically              |

4. Click **Deploy**. First build takes ~3 minutes (npm + Angular prod build).

## 4. Wire CORS back into the backend (1 min)

The backend currently allows no cross-origin calls. Since nginx proxies
`/api/**`, the browser only ever hits the **frontend** origin and CORS isn't
needed for the SPA. But the backend domain is also public (so Railway can
proxy nginx → backend), and a stricter setup tells Spring exactly who's
allowed to call it.

1. Go to the **backend** service → **Variables**.
2. Set `CORS_ALLOWED_ORIGINS` to the **frontend** domain, e.g.:
   `https://pharmacie-frontend-production.up.railway.app`
3. Railway redeploys automatically.

## 5. First login (1 min)

1. Open `https://<frontend-domain>` in the browser.
2. Log in with `admin` and the `ADMIN_PASSWORD` you generated.
3. Click the key icon in the top-right → **Change password** → set a real one.
4. Done — the app is live.

## 6. (Optional) Custom domain (5 min, requires owning a domain)

1. Buy a domain (Porkbun, Namecheap, OVH…).
2. In the frontend service → **Settings** → **Networking** → **Custom Domain**
   → paste your domain. Railway tells you which `CNAME` record to add at your
   registrar.
3. Add the `CNAME`, wait ~5 min for DNS propagation. Railway issues a
   Let's Encrypt certificate automatically.
4. **Important**: update `CORS_ALLOWED_ORIGINS` on the backend to the new
   custom domain (you can list both, comma-separated, during the transition).

---

## How to ship a code change later

```bash
# on your laptop
git add . && git commit -m "feat: …" && git push origin main
```

Railway detects the push, rebuilds the affected service, and rolls out a new
version. Zero-downtime deploys are on by default.

## How to roll back

In the service's **Deployments** tab, click the previous green deployment
→ **Redeploy**. Takes ~30 seconds.

## How to back up the database

Railway snapshots the Postgres volume nightly on the Hobby plan. To trigger
a manual on-demand backup or download a dump:

1. Open the Postgres service → **Data** tab → **Backups**.
2. Click **Create Backup** or **Download Snapshot**.

For an off-Railway copy, run from your laptop:

```bash
# replace with the values from the Postgres service "Variables" tab
PGPASSWORD=<password> pg_dump \
  -h <PGHOST> -p <PGPORT> -U <PGUSER> -d <PGDATABASE> \
  -F c -f pharmacie-$(date +%F).dump
```

## Cost expectation

| Item                              | Monthly cost            |
|-----------------------------------|-------------------------|
| Railway Hobby plan (includes $5 of usage) | $5                |
| 3 services running 24/7 with this workload | ≈ $3–5 on top      |
| **Total realistic**               | **~$5–10/month**        |

Idle hours don't cost much (Railway bills by RAM-seconds + CPU). For a
single-user pharmacy app the bill stays near the bottom of that range.

## Troubleshooting

- **Backend deploy fails on healthcheck** → check the **Deployments** logs.
  99% of the time it's a missing env var (typo in `ADMIN_PASSWORD`, or the
  Postgres plugin not attached to the same project).
- **Frontend shows blank page / 502 on `/api/`** → check that `BACKEND_URL`
  on the frontend service is the **https** URL of the backend, and that the
  backend healthcheck is green.
- **`Authentication required` after login on a fresh tab** → expected once,
  on first load Angular hits `/api/v1/auth/me` to check the session;
  if you weren't logged in yet you get bounced to `/login`. Log in once and
  the JSESSIONID cookie sticks.
- **Forgot the admin password** → go to the backend **Variables**, change
  `ADMIN_PASSWORD`, redeploy. The seeder only runs once *if no admin exists*,
  so for an existing user you also need to wipe the row:
  `DELETE FROM app_user WHERE username='admin';` from the Postgres **Data**
  tab, then redeploy.

