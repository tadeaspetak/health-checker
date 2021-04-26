# Service Health Checker

Simple service health checker.

## Run

You will need [Java](https://www.java.com/en/) and [Node & npm](https://nodejs.org/en/download/) installed. Clone this repo and `cd` into it.

In the first terminal window, run:

```bash
cd api
./gradlew run
```

In the second terminal window, run:

```bash
npm i -g yarn # that is, if you don't have yarn installed
cd client
yarn install
yarn start
```

Have fun 🎉

## Missing features & topics for discussion

1. Add a simple form of authentication to learn about middlewares / centralised filtering in `vertx`.
2. Instead of polling, use a socket, [most likely SockJS](https://vertx.io/blog/real-time-bidding-with-websockets-and-vert-x/).
3. Add animations; [Framer-Motion](https://www.framer.com/motion/) is my go-to library.
4. Make health checks cancellable to prevent pesky services that take a loooong time to respond from creating loose threads. Would love to discuss how this can be done in `vertx`.
