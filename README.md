1) Initial
   code [rev:2b8fcd50](https://github.com/Fuud/integration-tests-article/commit/2b8fcd509151ebc83c24d2b4e9fd0b665eb82ded)
2) Integration test module contains classpath from both
   microservices [rev:cfbebf68](https://github.com/Fuud/integration-tests-article/commit/cfbebf68c0876dc2bfaaca8cb3074d7c6275d414)
3) added security to client service, but worker service is affected in tests (
   only!) [rev:7c8abae7](https://github.com/Fuud/integration-tests-article/commit/7c8abae738f827b7601bc42704c3c1e657ae09fb)
4) Added maven-dependency-plugin:build-classpath and code to read resulting
   files [rev:50d2802f](https://github.com/Fuud/integration-tests-article/commit/50d2802f9f4cbf710beb65fbd87850139b3131d6)
5) Use nanocloud library: create separated jvm for each
   component [rev:f9118159](https://github.com/Fuud/integration-tests-article/commit/f9118159e514d15b9897104ec7b47e69b9e0c63d)
6) Add wrappers for ViNode, replace anonimous classes with
   lambdas. [rev:f7e1724b](https://github.com/Fuud/integration-tests-article/commit/f7e1724b9e8c35d976c8912c07444f1228af16b5)
7) Allocate free ports for each component. Try to preserve ports between
   runs. [rev:90080a1c](https://github.com/Fuud/integration-tests-article/commit/90080a1c51519a0bbf2126dda1b01fc69730db99)
8) Print config and http
   links [rev:a020e1f9](https://github.com/Fuud/integration-tests-article/commit/a020e1f92ea9e6fdce97671a598b3ecd32ffa93b)
9) Shutdown jvms after test