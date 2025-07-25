package blora.collection

class Stack<T> : Collection<T> {

    private val items = mutableListOf<T>()

    override val size: Int
        get() = this.items.size

    override fun isEmpty(): Boolean {
        return this.items.isEmpty()
    }

    override fun contains(element: T): Boolean {
        return this.items.contains(element)
    }

    override fun iterator(): Iterator<T> {
        return this.items.iterator()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return this.items.containsAll(elements)
    }

    fun get(): T {
        return this.items.last()
    }

    fun getOrNull(): T? {
        return this.items.lastOrNull()
    }

    fun popSafely(): T? {
        return this.items.removeLastOrNull()
    }

    fun popUntil(item: T, include: Boolean = false) {
        if (!this.contains(item))
            return
        val index = this.items.indexOf(item)
        if (index == -1)
            return
        this.items.removeIf { this.items.indexOf(it) > index }
        if (include)
            this.items.removeAt(index)
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