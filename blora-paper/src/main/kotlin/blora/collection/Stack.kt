package blora.collection

class Stack<T> {

    private val items = mutableListOf<T>()

    fun isEmpty() = items.isEmpty()

    fun get(): T {
        return this.items.last()
    }

    fun getOrNull(): T? {
        return this.items.lastOrNull()
    }

    fun popSafely(): T? {
        return this.items.removeLastOrNull()
    }

    fun push(item: T) {
        this.items.add(item)
    }

    fun replace(item: T): T? {
        val last = this.popSafely()
        this.push(item)
        return last
    }

    fun clear() {
        this.items.clear()
    }

}