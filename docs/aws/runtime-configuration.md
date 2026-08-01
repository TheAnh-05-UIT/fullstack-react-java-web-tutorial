# AWS runtime configuration contract

Sensitive values must be injected by ECS from Secrets Manager (or an equivalent
approved secret source). Non-sensitive values may be ECS environment variables.
Do not store resolved values in task-definition JSON committed to Git.

## Required application-service values

| Variable | Sensitive | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE=aws` | No | Selects managed runtime behavior |
| `SPRING_DATASOURCE_URL` | Usually no | RDS JDBC URL, including required TLS options |
| `SPRING_DATASOURCE_USERNAME` | Yes | Dedicated RDS application principal |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Application database password |
| `JWT_SECRET_BASE64` | Yes | Production JWT signing secret |
| `AWS_REGION` | No | Region used by the S3 client/provider chain |
| `APP_STORAGE_TYPE=s3` | No | Selects private S3 object storage |
| `APP_STORAGE_S3_BUCKET` | No | Private upload bucket name |

`JWT_ISSUER`, access/refresh audiences and expiration values retain their
existing production contract. No production fallback secret exists.

## Optional application-service values

| Variable | Default | Purpose |
| --- | --- | --- |
| `APP_STORAGE_S3_PREFIX` | `uploads/images` | Restricts application objects to a prefix |
| `DB_POOL_MAX_SIZE` | `10` | Maximum Hikari connections per ECS task |
| `DB_POOL_MIN_IDLE` | `2` | Minimum idle Hikari connections |
| `DB_POOL_CONNECTION_TIMEOUT_MS` | `30000` | Wait for a pool connection |
| `DB_POOL_VALIDATION_TIMEOUT_MS` | `5000` | Connection validation timeout |
| `DB_POOL_IDLE_TIMEOUT_MS` | `600000` | Idle connection retirement |
| `DB_POOL_MAX_LIFETIME_MS` | `1800000` | Maximum connection lifetime |
| `APP_SHUTDOWN_TIMEOUT` | `30s` | Graceful Spring shutdown window |

Size the maximum pool with all running and surge tasks in mind:
`task count × DB_POOL_MAX_SIZE` must remain below the RDS connection budget,
leaving capacity for migrations and operations. Keep `DB_POOL_MAX_LIFETIME_MS`
shorter than any network/database connection lifetime.

| Setting | Current base behavior | AWS default | Environment override |
| --- | ---: | ---: | --- |
| Maximum pool size | Hikari default | 10 | `DB_POOL_MAX_SIZE` |
| Minimum idle | Hikari default | 2 | `DB_POOL_MIN_IDLE` |
| Connection timeout | Hikari default | 30000 ms | `DB_POOL_CONNECTION_TIMEOUT_MS` |
| Validation timeout | Hikari default | 5000 ms | `DB_POOL_VALIDATION_TIMEOUT_MS` |
| Idle timeout | Hikari default | 600000 ms | `DB_POOL_IDLE_TIMEOUT_MS` |
| Max lifetime | Hikari default | 1800000 ms | `DB_POOL_MAX_LIFETIME_MS` |

## Migration task

Use the same image with:

```text
APP_RUNTIME_MODE=migration
SPRING_PROFILES_ACTIVE=aws,migration
SPRING_DATASOURCE_URL=<injected>
SPRING_DATASOURCE_USERNAME=<injected>
SPRING_DATASOURCE_PASSWORD=<injected>
```

The migration task does not require JWT, S3 bucket, or S3 permissions because it
starts only datasource and Flyway auto-configuration. A configuration or
migration error propagates as a non-zero process exit.

## Non-secret example

See [`runtime.env.example`](runtime.env.example). It is a contract example, not
a deployable task definition. Placeholder values must be replaced outside Git.
