data class Task(
    val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: Long,
    val priority: String,
    val list: String? = null,
    val trackedTimeMillis: Long = 0,
    val isTracking: Boolean = false,
    val trackingStartTime: Long = 0,
    val completed: Boolean = false
) 