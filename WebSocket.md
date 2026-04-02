## Hardware Connection

> URL: `ws://localhost:6612`
>
> Header: `Authorization: Bearer <token>` where `token` is the permanent token returned after exchange.
>
> Header: `X-Device-Id: <device_id>` where `device_id` is the device ID.
>
> Header: `X-App-Version: <app_version>` where `app_version` is the application version.

## Device Connection

> URL: `ws://localhost:6612/webrtc/signaling/client?id=<device_id>` where `device_id` is the target device ID.
>
> Header: `Authorization: Bearer <token>` where `token` is the login token returned by the API.
