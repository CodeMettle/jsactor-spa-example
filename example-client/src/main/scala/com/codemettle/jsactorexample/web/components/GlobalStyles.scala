package com.codemettle.jsactorexample.web.components

import japgolly.scalacss.Defaults._

/**
 * @author steven
 *
 */
object GlobalStyles extends StyleSheet.Inline {
    import dsl._

    style(unsafeRoot("body")(
        paddingTop(50.px)
    ))

    val bootstrapStyles = new BootstrapStyles
}
