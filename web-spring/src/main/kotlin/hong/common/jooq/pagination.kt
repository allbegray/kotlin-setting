package hong.common.jooq

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectLimitStep
import kotlin.math.ceil
import kotlin.math.min

private fun intCeil(x: Int, y: Int): Int = ceil(x.toDouble() / y).toInt()

class Pagination<E>(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val content: List<E>,
    val totalPages: Int,
    val pages: List<Int>,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasContent: Boolean,
    private val offset: Int
) {

    data class Entry<E>(val index: Int, val item: E)

    val indexedItems by lazy { content.mapIndexed { index, e -> Entry(total - offset - index, e) } }

    companion object {
        fun <R : Record, E> of(
            ctx: DSLContext,
            query: SelectLimitStep<R>,
            mapper: (record: R) -> E,
            page: Int,
            pageSize: Int = 20,
            navSize: Int = 10
        ): Pagination<E> {
            val total = ctx.fetchCount(query)
            val offset = (page - 1) * pageSize
            val content = query.limit(offset, pageSize).map(mapper)

            val totalPages = if (pageSize == 0) 1 else ceil(total.toDouble() / pageSize).toInt()
            val hasPrevious = page > 1
            val hasNext = page < totalPages
            val isFirst = !hasPrevious
            val isLast = !hasNext
            val hasContent = content.isNotEmpty()

            val navHead = navSize * (ceil(page.toDouble() / navSize).toInt() - 1) + 1
            val navTail = min(totalPages, navHead + navSize - 1)

            return Pagination(
                page,
                pageSize,
                total,
                content,
                totalPages,
                (navHead..navTail).toList(),
                hasPrevious,
                hasNext,
                isFirst,
                isLast,
                hasContent,
                offset
            )
        }
    }
}