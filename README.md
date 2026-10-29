# Ark KVM Cloud API

> [!WARNING]
> **This project is deprecated and no longer maintained.**
>
> ArkKVM Cloud has been rewritten in Go. The new codebase is an independent
> project — it is not a fork of this repository — and now lives at:
>
> **[arkkvm/arkkvm-cloud-go](https://github.com/arkkvm/arkkvm-cloud-go)**
>
> No further changes, fixes, or releases will be made here. This repository is
> kept only for historical reference.

## Requirements

- Java: 8
- MySQL: 8.x recommended. The project uses `mysql-connector-j` `8.2.0`.
- Redis: 6.x or 7.x recommended. The repository config does not pin a Redis server version, but the current Spring Boot + Lettuce setup is intended for a modern standalone Redis deployment.

## Build

Use the Maven wrapper included in the repository.

Build:

```bash
./mvnw clean package
```

Run tests only:

```bash
./mvnw test
```

## Third-Party Setup

### Google OAuth2

Create the OAuth client in Google Cloud Console:

- Console entry: https://console.cloud.google.com/apis/credentials
- Guide: https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid
- Choose `Web application` as the client type.
- Add your backend callback URL to `Authorized redirect URIs`, for example:
  - `https://your-domain.com/oauth2/code/google`
  - `http://localhost:6612/oauth2/code/google`

### WebRTC / TURN

This project uses Cloudflare Realtime TURN credentials.

- Dashboard/docs entry: https://developers.cloudflare.com/realtime/turn/generate-credentials/
- You need to create a TURN key in the Cloudflare Dashboard or via API.
- Store the generated `turn-key-id` and `turn-key-api-token` in your application config.

## Generate the Keystore

The project loads a `PKCS12` keystore and uses its private key for `SHA256withRSA` signing.

Example command:

```bash
keytool -genkeypair \
  -alias arkkvm \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -storetype PKCS12 \
  -keystore server.p12 \
  -validity 3650 \
  -storepass arkkvm \
  -keypass arkkvm \
  -dname "CN=ArkKVM, OU=Dev, O=ArkKVM, L=Shanghai, ST=Shanghai, C=CN"
```

After generating the file, point the application config to it:

```yaml
sign:
  keystore:
    path: "file:/absolute/path/to/server.p12"
    password: arkkvm
```

Notes:

- The keystore type must be `PKCS12`.
- The password in `sign.keystore.password` must match the keystore password.
- The current code reads the first alias from the keystore, so a single-key keystore is the safest setup.

## Local Run

The default application port is `6612`.

Before starting the service, copy the template and create your own profile file.

```bash
cp src/main/resources/application-template.yaml src/main/resources/application-local.yaml
```

Then fill in your own MySQL, Redis, Google OAuth2, Cloudflare TURN, JWT, cookie, CDN, and keystore values.

1. Prepare MySQL.

- Create a database such as `ark-kvm`.
- Update `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` in your active profile config.

2. Prepare Redis.

- Start a standalone Redis instance.
- Update `spring.redis.host`, `spring.redis.port`, `spring.redis.password`, and `spring.redis.database`.

3. Generate a keystore.

- Create a `server.p12` file with the command shown above.
- Set `sign.keystore.path` to the absolute file path.
- Set `sign.keystore.password` to the same password used when creating the keystore.

4. Prepare Google OAuth2.

- Create a Google OAuth client.
- Fill `oauth2.google.client-id` and `oauth2.google.client-secret`.
- Set the redirect URI to your backend callback, for example `http://localhost:6612/oauth2/code/google`.

5. Prepare Cloudflare TURN credentials.

- Create a TURN key in Cloudflare.
- Fill `webrtc.cloudflare.turn-key-id` and `webrtc.cloudflare.turn-key-api-token`.

6. Choose the active profile.

- The project no longer ships with ready-to-use `application-local.yaml` or `application-dev.yaml`.
- You must create your own profile file from the template.
- Select the profile before startup, for example:

```bash
export ENV_MODE=local
```

7. Start the application.

```bash
./mvnw spring-boot:run
```

Or run the packaged jar:

```bash
./mvnw clean package
java -jar ./target/ark-kvm-cloud-api.jar
```

8. Verify the service.

- Base URL: `http://localhost:6612`
- OAuth callback: `http://localhost:6612/oauth2/code/google`
- WebSocket signaling path: `ws://localhost:6612/webrtc/signaling/client`
- Knife4j page: `http://localhost:6612/doc.html`

## Notes

- Google OAuth2 client credentials are configured under `oauth2.google`.
- Cloudflare TURN settings are configured under `webrtc.cloudflare`.
- Keystore settings are configured under `sign.keystore`.
- Copy `src/main/resources/application-template.yaml` into your own profile file before running the project.
- Redis, MySQL, OAuth, and TURN secrets should be moved out of committed config before production use.
