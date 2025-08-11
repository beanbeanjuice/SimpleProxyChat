rootProject.name = "SimpleProxyChat"

include(
    "projects/proxy",
    "projects/server"
)

project(":projects/proxy").name = "SimpleProxyChat"
project(":projects/server").name = "SimpleProxyChatHelper"
