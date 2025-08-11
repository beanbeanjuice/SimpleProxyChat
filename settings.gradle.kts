rootProject.name = "SimpleProxyChat"

include(
    "projects:common",
    "projects:proxy:common",
    "projects:server:common"
)

project(":projects:proxy:common").name = "SimpleProxyChat"
project(":projects:server:common").name = "SimpleProxyChatHelper"
