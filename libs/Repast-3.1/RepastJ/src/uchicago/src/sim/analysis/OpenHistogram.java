/*$$
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Chicago nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/
package uchicago.src.sim.analysis;

import java.util.List;
import java.util.Vector;

import uchicago.src.sim.analysis.plot.OpenGraph;
import uchicago.src.sim.engine.SimModel;

/**
 * A dynamic bar chart. The OpenHistogram class allows the user to histogram data
 * generated by a collection of objects. The histogram range, as displayed, is
 * [lowerBound, maxValue] where the maxValue is calculated each time the graph
 * is stepped. The individual bin ranges except for the final bin are
 * [Math.floor(value), Math.ceil(value + interval)) where interval
 * is calculated by (maxValue - lowerBound) / numBins. The final bin is
 * inclusive.
 *
 * @author Nick Collier
 * @version $Revision: 1.8 $ $Date: 2004/11/03 19:51:00 $
 * @see HistogramStatistic
 * @see HistogramItem
 */
public class OpenHistogram extends OpenGraph {

  public OpenHistogramStat stats;
  public int dataset = 0;

  /**
   * Constructs an OpenHistogram with the specified title, the specified number
   * of bins, and the specified lowerBound. A lowerBound higher than any
   * expected data values will cause that data not to be included in the
   * histogram.
   *
   * @param title the title of the histogram
   * @param numBins the number of bins
   * @param lowerBound the lower bound of the histogram
   */

  public OpenHistogram(String title, int numBins, long lowerBound) {
    super(title);
    stats = new OpenHistogramStat(numBins, lowerBound);
    this.setBars(.5, .2);
    this.setXRange(0, numBins - 1);
  }

  /**
   * Constructs an OpenHistogram with the specified title, the specified number
   * of bins, and the specified lowerBound. A lowerBound higher than any
   * expected data values will cause that data not to be included in the
   * histogram. Model parameter necessary if you want to take snapshots or
   * movies of this graph.
   *
   * @param title the title of the histogram
   * @param numBins the number of bins
   * @param lowerBound the lower bound of the histogram
   * @param model the model associated with this graph
   */
  public OpenHistogram(String title, int numBins, long lowerBound, SimModel model) {
    this(title, numBins, lowerBound);
    this.model = model;
  }

  /**
   * Creates a HistogramItem with specified name, list and BinDataSource to be
   * displayed by this Histogram.
   * The HistogramItem iterates over the specified list passing each Object
   * in the list as argument to the getBinValue method of the BinDataSource.
   * This getBinValue method returns a double. All these doubles
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * <b>Note:</b> This creates a Histogram that updates faster than
   * those created with createHistogramItem(String name, List list, String
   * methodName). Consequently, it should be preferred over the other method.
   *
   * @param name the name of the item
   * @param list the list of objects that provided the data for the item
   * @param source the BinDataSource used to get the data from the objects
   * in the list.
   */

  public void createHistogramItem(String name, List list, BinDataSource source) {
    stats.createHistogramItem(name, list, source);
    plot.addLegend(dataset++, name);
  }

  /**
   * Creates a HistogramItem with specified name to be displayed by this
   * OpenHistogram.
   * The HistogramItem iterates over the specified list passing each Object
   * in the list as argument to the method with the specified name. This
   * method is a method of the specified object and returns a double. All these
   *  doubles
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * @param name the name of the item
   * @param list the list of objects that provided the data for the item
   * @param target the target of the name method
   * @param methodName the name of the method to call and pass each object
   * in the list to
   */
  public void createHistogramItem(String name, List list, Object target,
                                  String methodName) {
    BinDataSource bds = createBinDataSource(target, methodName);
    createHistogramItem(name, list, bds);
  }

  /**
   * Creates a HistogramItem with specified name to be displayed by this
   * OpenHistogram.
   * The HistogramItem iterates over the specified list passing each Object
   * in the list as argument to the method with the specified name. This
   * method is a method of the specified object and returns a double. All these
   *  doubles
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * @param name the name of the item
   * @param list the list of objects that provided the data for the item
   * @param target the target of the name method
   * @param methodName the name of the method to call and pass each object
   * in the list to
   * @param maxIntegerDigits the maximum number of digits before the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   * @param maxFractionDigits the maximum number of digits after the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   */
  public void createHistogramItem(String name, List list, Object target,
                                  String methodName, int maxIntegerDigits,
                                  int maxFractionDigits) {
    BinDataSource bds = createBinDataSource(target, methodName);
    createHistogramItem(name, list, bds, maxIntegerDigits, maxFractionDigits);
  }

  /**
   * Creates a HistogramItem with specified name, list and BinDataSource to be
   * displayed by this Histogram.
   * The HistogramItem iterates over the specified list passing each Object
   * in the list as argument to the getBinValue method of the BinDataSource.
   * This getBinValue method returns a double. All these doubles
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * @param name the name of the item
   * @param list the list of objects that provided the data for the item
   * @param source the BinDataSource used to get the data from the objects
   * in the list.
   * @param maxIntegerDigits the maximum number of digits before the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   * @param maxFractionDigits the maximum number of digits after the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   */

  public void createHistogramItem(String name, List list, BinDataSource source,
                                  int maxIntegerDigits, int maxFractionDigits) {
    stats.createHistogramItem(name, list, source, maxIntegerDigits,
                              maxFractionDigits);
    plot.addLegend(dataset++, name);
  }


  /**
   * Creates a HistogramItem to be displayed by this OpenHistogram. The HistogramItem
   * iterates over the specified list calling the specified method on each
   * object in the list. This method must return a value that can be
   * cast into a double (i.e. float, int, double, short, etc.). These values
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * <b>Note:</b> This creates a Histogram that updates more slowly than
   * that created with createHistogramItem(String name, List list,
   * BinDataSource source). Consequently, this method should not be used
   * unless the Histogram will be updated infrequently.
   *
   * @param name the name of this item
   * @param list the list of object on which the specified method is called
   * @param listObjMethodName the name of the method to call on the objects. Should
   * return a Number value.
   *
   * @see HistogramItem
   */
  public void createHistogramItem(String name, List list,
                                  String listObjMethodName) {
    stats.createHistogramItem(name, list, listObjMethodName);
    plot.addLegend(dataset++, name);
  }

  /**
   * Creates a HistogramItem to be displayed by this OpenHistogram. The HistogramItem
   * iterates over the specified list calling the specified method on each
   * object in the list. This method must return a value that can be
   * cast into a double (i.e. float, int, double, short, etc.). These values
   * are then distributed across the bins according to the number of bins,
   * the lower bound, and the maximum value. For example, given 2 bins,
   * a lower bound of 0 and a maximum value of 4. The first bin will contain
   * all the values from 0 up to but not including 2, and the final bin will
   * contain all the values from 2 up to <em>and</em> including 4. The displayed
   * bin value (i.e. the height of the bar in the chart) is the number of values
   * that fall within this bin.<p>
   *
   * <b>Note:</b> This creates a Histogram that updates more slowly than
   * that created with createHistogramItem(String name, List list,
   * BinDataSource source). Consequently, this method should not be used
   * unless the Histogram will be updated infrequently.
   *
   * @param name the name of this item
   * @param list the list of object on which the specified method is called
   * @param listObjMethodName the name of the method to call on the objects. Should
   * return a Number value.
   * @param maxIntegerDigits the maximum number of digits before the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   * @param maxFractionDigits the maximum number of digits after the
   * decimal point in the bin labels. A value of -1 will record all the digits.
   * @see HistogramItem
   */
  public void createHistogramItem(String name, List list,
                                  String listObjMethodName,
                                  int maxIntegerDigits, int maxFractionDigits) {
    stats.createHistogramItem(name, list, listObjMethodName, maxIntegerDigits,
                              maxFractionDigits);
    plot.addLegend(dataset++, name);
  }

  /**
   * Records data from the HistogramItems without updating the display
   */

  public void record() {
    stats.record();
  }

  /**
   * Records any new data and updates the displayed graph.
   */
  public void step() {
    stats.record();
    updateGraph();
  }

  /**
   * Updates the graph.
   */
  public void updateGraph() {

    plot.clearPoints();

    String[] labels = stats.getPointLabels();
    for (int i = 0; i < labels.length; i++) {
      this.updateXTick(i, labels[i], i);
    }

    Vector table = stats.getDataTable();
    Vector xVals = (Vector) table.get(0);
    for (int i = 0; i < xVals.size(); i++) {
      double xVal = ((Double) xVals.get(i)).doubleValue();

      for (int j = 1; j < table.size(); j++) {
        Vector yVals = (Vector) table.get(j);
        double yVal = ((Double) yVals.get(i)).doubleValue();

        plot.addPoint(j - 1, xVal, yVal, false);
      }
    }

    //plot.repaint();
    plot.fillPlot();
  }
}
