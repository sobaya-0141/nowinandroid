package sobaya.sample.repository

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform