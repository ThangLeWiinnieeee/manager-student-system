# Render Free + Supabase

## Current readiness

The Dockerfile builds and tests the executable WAR. `render.yaml` selects the free
plan and the `render` Spring profile. This profile enables HTTPS session cookies,
proxy headers, hides error details and limits the database pool to five connections.
The initial JVM memory limits must still be checked under actual Render load.

Deployment is not yet complete: cloud credentials and live verification are pending.
With `BREVO_API_KEY` set, password recovery sends through the Brevo HTTPS API;
without it, local SMTP remains available. With `SUPABASE_URL` and
`SUPABASE_SERVICE_ROLE_KEY` set, reports use private Supabase Storage; without
them, local disk remains available only outside the `render` profile. The `render` profile refuses to start without both settings, preventing accidental temporary storage. Render Free
blocks SMTP ports 25/465/587 and does not persist local uploads across restarts.

## Account preparation

1. Sign in to Render with GitHub and grant access to this repository.
2. In Supabase, open **Connect > Session pooler** and copy the host, database and
   username. Use port 5432 (session mode), not the transaction pooler on 6543.
3. Prepare these Render environment variables; never commit their real values:

   | Variable | Value |
   | --- | --- |
   | `DB_URL` | `jdbc:postgresql://<session-pooler-host>:5432/postgres?sslmode=require` |
   | `DB_USERNAME` | Exact username shown by Supabase, typically `postgres.<project-ref>` |
   | `DB_PASSWORD` | Supabase database password, not an API key |

4. If using a new empty database, also set `ADMIN_EMAIL` and a strong
   `ADMIN_PASSWORD`. Existing accounts are retained; these settings do not reset
   an existing administrator password.
5. Create a **private** Supabase Storage bucket named `reports`. Keep the project
   URL as `SUPABASE_URL` and the legacy `service_role` JWT as
   `SUPABASE_SERVICE_ROLE_KEY` in Render environment settings only. Do not use an
   anon/publishable key. Set `SUPABASE_STORAGE_BUCKET=reports`. Allow ZIP uploads (`application/zip`) and legacy PDFs (`application/pdf`)
   up to 10 MB. Existing local reports must be copied into this bucket with their
   stored UUID filenames before switching storage; this is not automatic.
6. Create a Brevo account, activate transactional email and verify a sender
   (authenticate its domain if requested by Brevo). Set `BREVO_API_KEY` and
   `MAIL_FROM` in Render. The sender must be approved by Brevo; account approval
   and recipient delivery are not guaranteed until tested. Do not send keys in chat.

## Create the web service after integrations are ready

Push the deployment changes to GitHub. In Render select **New > Blueprint**, choose
the repository and the branch containing `render.yaml`, then enter the database
secrets. Alternatively create a Docker Web Service on the Free plan and copy the
environment settings from `render.yaml`. Do not create a Render database.

Startup runs Flyway migrations against the configured database. Confirm the target
project and preserve a backup before deploying to an existing database. Render does
not load the local `.env` file; enter settings in its dashboard.

## Verify the public deployment

- Build and startup succeed, database migrations finish, and `/login` returns 200.
- Login/logout work over HTTPS for the relevant roles.
- Request a real OTP, verify it and reset a test account's password.
- Upload and download a ZIP as authorized users; unrelated users are denied.
- Restart the service and confirm the uploaded ZIP is still downloadable.
- Check the core registration, grading and publication flow using test records.

Render Free sleeps after inactivity; the next request can take longer. This setup
is suitable for a demo, not a promise of uninterrupted availability.

Rollback cleanup is best-effort: an upload followed by a database rollback may
leave an orphan object if the delete request also fails. Files already referenced
by database records are never automatically removed.

Sources: https://developers.brevo.com/docs/send-a-transactional-email,
https://render.com/docs/free,
https://supabase.com/docs/guides/database/connecting-to-postgres
