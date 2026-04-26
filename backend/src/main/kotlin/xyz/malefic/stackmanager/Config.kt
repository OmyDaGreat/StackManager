package xyz.malefic.stackmanager

object Config {
    val token: String = System.getenv("STACKMGR_TOKEN") ?: error("STACKMGR_TOKEN env var is required")
    val bindHost: String = System.getenv("STACKMGR_BIND_HOST") ?: "127.0.0.1"
    val port: Int = System.getenv("STACKMGR_PORT")?.toIntOrNull() ?: 8080
    val webRoot: String = System.getenv("STACKMGR_WEB_ROOT") ?: "/app/public"
    const val COMPOSE_ROOT = "/srv/compose"
}
