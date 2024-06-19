rootProject.name = "SimpleProxyChatMaster"

include(
    "projects:main-app",
    "projects:helper-app"
)

project(":projects:main-app").name = "SimpleProxyChat"
project(":projects:helper-app").name = "SimpleProxyChatHelper"
