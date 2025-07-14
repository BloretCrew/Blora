package blora.collection

class Stack<T> {

    private val items = mutableListOf<T>()

    fun isEmpty() = items.isEmpty()

    fun get(): T {
        return this.items.last()
    }

    fun pop() {
        this.items.removeLast()
    }

    fun push(item: T) {
        this.items.add(item)
    }

    fun replace(item: T) {
        this.items.removeLast()
        this.items.add(item)
    }

}