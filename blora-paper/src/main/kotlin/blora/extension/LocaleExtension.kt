package blora.extension

import java.util.*


fun Locale.toStringTag(): String {
    return buildString {
        append(language.lowercase()) // 语言代码转小写
        if (country.isNotEmpty()) {
            append('_') // 添加下划线分隔符
            append(country.lowercase()) // 国家代码转小写
        }
    }
}