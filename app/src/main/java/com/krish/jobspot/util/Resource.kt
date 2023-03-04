package com.krish.jobspot.util

/**
 * Enum class representing the status of a resource.
 */
enum class Status {
    SUCCESS, // Indicates that a resource was successfully loaded.
    ERROR, // Indicates that there was an error while loading the resource.
    LOADING // Indicates that a resource is currently being loaded.
}

/**
 * A generic class that represents a resource, such as a network response.
 *
 * @param T The type of the resource data.
 * @property status The current status of the resource.
 * @property data The resource data.
 * @property message A message associated with the resource, if any.
 */
data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {
    companion object {
        /**
         * Creates a [Resource] object representing a successful resource load.
         *
         * @param data The resource data.
         * @return A [Resource] object representing a successful resource load.
         */
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
        /**
         * Creates a [Resource] object representing an error while loading a resource.
         *
         * @param msg A message associated with the error.
         * @return A [Resource] object representing an error while loading a resource.
         */
        fun <T> error(msg: String): Resource<T> {
            return Resource(Status.ERROR, null, msg)
        }

        /**
         * Creates a [Resource] object representing a resource that is currently being loaded.
         *
         * @return A [Resource] object representing a resource that is currently being loaded.
         */
        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }
    }
}