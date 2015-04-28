package com.codemettle.jsactorexample.web.modules

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import com.codemettle.jsactorexample.web.components.{ChartData, ChartDataset, Chart, Motd}

/**
 * @author steven
 *
 */
object Dashboard {
  val component = ReactComponentB[MainRouter.Router]("Dashboard").render(router ⇒ {

    val cp = Chart.ChartProps("Test chart", Chart.BarChart, ChartData(Seq("A", "B", "C"), Seq(ChartDataset(Seq(1, 2, 3), "Data1"))))

    <.div(
      <.h2("Dashboard"),
      Motd(),
      Chart(cp),
      // link to Todo
      <.div(MainRouter.todoLink("Check your todos!"))
    )
  }).build
}
