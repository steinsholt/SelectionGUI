package com.sff.report_performance;

import java.awt.Color;

import com.moyosoft.connector.ms.excel.AxisType;
import com.moyosoft.connector.ms.excel.Chart;
import com.moyosoft.connector.ms.excel.ChartObject;
import com.moyosoft.connector.ms.excel.ChartType;
import com.moyosoft.connector.ms.excel.Range;
import com.moyosoft.connector.ms.excel.Worksheet;


public class ExcelHelper {

	public static void create3DPieChart(int left, int top, int width, int height, Worksheet sheet, Range source, Range names){
		ChartObject chartObject = sheet.getChartObjects().add(left, top, width, height);
		Chart chart = chartObject.getChart();
		chart.setChartType(ChartType.PIE_EXPLODED_3D);
		chart.setSourceData(source);
		chart.getAxis(AxisType.CATEGORY).setCategoryNames(names);
		chart.getSeries(0).setHasDataLabels(true);
		chart.getSeries(0).getDataLabels().setShowPercentage(true);
		chart.getSeries(0).getDataLabels().setShowValue(false);
		chart.getSeries(0).getDataLabels().setShowCategoryName(true);
		chart.getSeries(0).setHasLeaderLines(true);
		chart.getSeries(0).getLeaderLines().getBorder().setColor(Color.black);
		chart.getPie3DGroup().setHas3DShading(true);
		chart.setElevation(60);
		chart.setRotation(60);
		chart.setHasLegend(false);
	}
}
