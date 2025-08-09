rootProject.name = "SimpleProxyChat"

include(
    "projects/SimpleProxyChat",
    "projects/SimpleProxyChatHelper"
)

project(":projects/SimpleProxyChat").name = "SimpleProxyChat"
project(":projects/SimpleProxyChatHelper").name = "SimpleProxyChatHelper"
