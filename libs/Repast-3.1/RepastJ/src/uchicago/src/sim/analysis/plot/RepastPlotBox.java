/* A labeled box for signal plots.

@Author: Edward A. Lee and Christopher Hylands

@Contributors:  William Wu

@Copyright (c) 1997-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/
package uchicago.src.sim.analysis.plot;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;

import ptolemy.plot.EPSGraphics;
import ptolemy.plot.Message;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;

/**
 * This class provides a labeled box within which to place a data plot.
 * A title, X and Y axis labels, tick marks, and a legend are all supported.
 * Zooming in and out is supported.  To zoom in, drag the mouse
 * downwards to draw a box.  To zoom out, drag the mouse upward.
 * <p>
 * The box can be configured either through a file with commands or
 * through direct invocation of the public methods of the class.
 * <p>
 * When calling the methods, in most cases the changes will not
 * be visible until paint() has been called.  To request that this
 * be done, call repaint().
 * <p>
 * A small set of key bindings are provided for convenience.
 * They are:
 * <ul>
 * <li> Cntr-c: Export the plot to the clipboard (in PlotML).
 * <li> D: Dump the plot to standard output (in PlotML).
 * <li> E: Export the plot to standard output in EPS format.
 * <li> F: Fill the plot.
 * <li> H or ?: Display a simple help message.
 * <li> Cntr-D or Q: quit
 * </ul>
 * These commands are provided in a menu by the PlotFrame class.
 * <p>
 * At this time, the two export commands produce encapsulated postscript
 * tuned for black-and-white printers.  In the future, more formats may
 * supported.  Also at this time (jdk 1.2), Java's interface the
 * clipboard does not work, so Cntr-c might not accomplish anything.
 * Exporting to the clipboard and to standard output, in theory,
 * is allowed for applets, unlike writing to a file. Thus, these
 * key bindings provide a simple mechanism to obtain a high-resolution
 * image of the plot from an applet, suitable for incorporation in
 * a document. However, in some browsers, exporting to standard out
 * triggers a security violation.  You can use Sun's appletviewer instead.
 * <p>
 * To read commands from a file or URL, the preferred technique is
 * to use one of the classes in the plotml package.  That package
 * supports both PlotML, an XML extension for plots, and a historical
 * file format specific to ptplot.  The historical file format is
 * understood by the read() method in this class.
 * The syntax of the historical format, documented below, is rudimentary,
 * and will probably not be extended as ptplot evolves.  Nonetheless,
 * we document it here since it is directly supported by this class.
 * <p>
 * The historical format for the file allows any number
 * commands, one per line.  Unrecognized commands and commands with
 * syntax errors are ignored.  Comments are denoted by a line starting
 * with a pound sign "#".  The recognized commands include:
 * <pre>
 * TitleText: <i>string</i>
 * XLabel: <i>string</i>
 * YLabel: <i>string</i>
 * </pre>
 * These commands provide a title and labels for the X (horizontal) and Y
 * (vertical) axes.
 * A <i>string</i> is simply a sequence of characters, possibly
 * including spaces.  There is no need here to surround them with
 * quotation marks, and in fact, if you do, the quotation marks will
 * be included in the labels.
 * <p>
 * The ranges of the X and Y axes can be optionally given by commands like:
 * <pre>
 * XRange: <i>min</i>, <i>max</i>
 * YRange: <i>min</i>, <i>max</i>
 * </pre>
 * The arguments <i>min</i> and <i>max</i> are numbers, possibly
 * including a sign and a decimal point. If they are not specified,
 * then the ranges are computed automatically from the data and padded
 * slightly so that datapoints are not plotted on the axes.
 * <p>
 * The tick marks for the axes are usually computed automatically from
 * the ranges.  Every attempt is made to choose reasonable positions
 * for the tick marks regardless of the data ranges (powers of
 * ten multiplied by 1, 2, or 5 are used).  However, they can also be
 * specified explicitly using commands like:
 * <pre>
 * XTicks: <i>label position, label position, ...</i>
 * YTicks: <i>label position, label position, ...</i>
 * </pre>
 * A <i>label</i> is a string that must be surrounded by quotation
 * marks if it contains any spaces.  A <i>position</i> is a number
 * giving the location of the tick mark along the axis.  For example,
 * a horizontal axis for a frequency domain plot might have tick marks
 * as follows:
 * <pre>
 * XTicks: -PI -3.14159, -PI/2 -1.570795, 0 0, PI/2 1.570795, PI 3.14159
 * </pre>
 * Tick marks could also denote years, months, days of the week, etc.
 * <p>
 * The X and Y axes can use a logarithmic scale with the following commands:
 * <pre>
 * XLog: on
 * YLog: on
 * </pre>
 * The grid labels represent powers of 10.  Note that if a logarithmic
 * scale is used, then the values must be positive.  Non-positive values
 * will be silently dropped.
 * <p>
 * By default, tick marks are connected by a light grey background grid.
 * This grid can be turned off with the following command:
 * <pre>
 * Grid: off
 * </pre>
 * It can be turned back on with
 * <pre>
 * Grid: on
 * </pre>
 * Also, by default, the first ten data sets are shown each in a unique color.
 * The use of color can be turned off with the command:
 * <pre>
 * Color: off
 * </pre>
 * It can be turned back on with
 * <pre>
 * Color: on
 * </pre>
 * Finally, the rather specialized command
 * <pre>
 * Wrap: on
 * </pre>
 * enables wrapping of the X (horizontal) axis, which means that if
 * a point is added with X out of range, its X value will be modified
 * modulo the range so that it lies in range. This command only has an
 * effect if the X range has been set explicitly. It is designed specifically
 * to support oscilloscope-like behavior, where the X value of points is
 * increasing, but the display wraps it around to left. A point that lands
 * on the right edge of the X range is repeated on the left edge to give
 * a better sense of continuity. The feature works best when points do land
 * precisely on the edge, and are plotted from left to right, increasing
 * in X.
 * <p>
 * All of the above commands can also be invoked directly by calling the
 * the corresponding public methods from some Java procedure.
 * <p>
 * This class uses features of JDK 1.1, and hence if used in an applet,
 * it can only be viewed by a browser that supports JDK 1.1.
 * <p>
 * This is almost entirely a cut and paste from Ptolemy's Plot class.
 * This approach was necessary as the scope of some methods needed to
 * be changed. See the Ptolemy docs for more information.
 *
 * @author Edward A. Lee, Christopher Hylands
 * @contributor Jun Wu (jwu@inin.com.au)
 * @contributor Nick Collier
 * @version $Id: RepastPlotBox.java,v 1.10 2004/11/03 19:51:02 jerryvos Exp $
 */
public class RepastPlotBox extends JPanel {

  public ZoomListener zListener;
  public ArrayList _detailsStrings = new ArrayList();

  ///////////////////////////////////////////////////////////////////
  ////                         constructor                       ////

  /** Construct a plot box with a default configuration. */
  public RepastPlotBox(ZoomListener listener) {
    super(false);
    setLayout(new FlowLayout(FlowLayout.RIGHT));
    addMouseListener(new PtZoomListener());
    addKeyListener(new CommandListener());
    addMouseMotionListener(new DragListener());
    // This is something we want to do only once...
    _measureFonts();
    // Request the focus so that key events are heard.
    requestFocus();
    zListener = listener;
  }

  ///////////////////////////////////////////////////////////////////
  ////                         public methods                    ////

  /** Add a legend (displayed at the upper right) for the specified
   *  data set with the specified string.  Short strings generally
   *  fit better than long strings.  If the string is empty, or the
   *  argument is null, then no legend is added.
   *  @param dataset The dataset index.
   *  @param legend The label for the dataset.
   */
  public void addLegend(int dataset, String legend) {
    if (legend == null || legend.equals("")) return;
    _legendStrings.addElement(legend);
    _legendDatasets.addElement(new Integer(dataset));
    int color = dataset % _colors.length;
    dsColorMap.put(dataset, _colors[color]);
    dsMarkMap.put(dataset, dataset % _MAX_MARKS);
  }


  public void addLegend(int dataset, String legend, Color color) {
    addLegend(dataset, legend);
    dsColorMap.put(dataset, color);
  }

  public void addLegend(int dataset, String legend, int markStyle) {
    addLegend(dataset, legend);
    dsMarkMap.put(dataset, markStyle);
  }

  public void addLegend(int dataset, String legend, Color color,
                        int markStyle) {
    addLegend(dataset, legend);
    dsColorMap.put(dataset, color);
    dsMarkMap.put(dataset, markStyle);
  }

  /**
   * Adds a String to be displayed to the right of the plot
   *
   * @param detail the String to be displayed
   */
  public void addDetailString(String detail) {
    _detailsStrings.add(detail);
  }

  /**
   * Clears the list of Strings to be displayed to the right of the plot.
   */
  public void clearDetailStrings() {
    _detailsStrings.clear();
  }

  /** Specify a tick mark for the X axis.  The label given is placed
   *  on the axis at the position given by <i>position</i>. If this
   *  is called once or more, automatic generation of tick marks is
   *  disabled.  The tick mark will appear only if it is within the X
   *  range.
   *  @param label The label for the tick mark.
   *  @param position The position on the X axis.
   */
  public void addXTick(String label, double position) {
    if (_xticks == null) {
      _xticks = new Vector();
      _xticklabels = new Vector();
    }
    _xticks.addElement(new Double(position));
    _xticklabels.addElement(label);
  }

  public void updateXTick(String label, double position, int index) {
    if (_xticks == null) {
      _xticks = new Vector();
      _xticklabels = new Vector();
    }

    if (_xticks.size() > index) {
      _xticks.removeElementAt(index);
      _xticklabels.removeElementAt(index);
    }

    _xticks.insertElementAt(new Double(position), index);
    _xticklabels.insertElementAt(label, index);
  }


  /** Specify a tick mark for the Y axis.  The label given is placed
   *  on the axis at the position given by <i>position</i>. If this
   *  is called once or more, automatic generation of tick marks is
   *  disabled.  The tick mark will appear only if it is within the Y
   *  range.
   *  @param label The label for the tick mark.
   *  @param position The position on the Y axis.
   */
  public void addYTick(String label, double position) {
    if (_yticks == null) {
      _yticks = new Vector();
      _yticklabels = new Vector();
    }
    _yticks.addElement(new Double(position));
    _yticklabels.addElement(label);
  }

  /** If the argument is true, clear the axes.  I.e., set all parameters
   *  controlling the axes to their initial conditions.
   *  For the change to take effect, call repaint().  If the argument
   *  is false, do nothing.
   *  @param axes If true, clear the axes parameters.
   */
  public synchronized void clear(boolean axes) {
    _xBottom = Double.MAX_VALUE;
    _xTop = -Double.MAX_VALUE;
    _yBottom = Double.MAX_VALUE;
    _yTop = -Double.MAX_VALUE;
    if (axes) {
      // public members first.
      _yMax = 0;
      _yMin = 0;
      _xMax = 0;
      _xMin = 0;
      _xRangeGiven = false;
      _yRangeGiven = false;
      _rangesGivenByZooming = false;
      _xlog = false;
      _ylog = false;
      _grid = true;
      _wrap = false;
      _usecolor = true;

      // public members next...
      _filespec = null;
      _xlabel = null;
      _ylabel = null;
      _title = null;
      _legendStrings = new Vector();
      _legendDatasets = new Vector();
      _xticks = null;
      _xticklabels = null;
      _yticks = null;
      _yticklabels = null;
    }
  }

  /** Export a description of the plot.
   *  Currently, only EPS is supported.  But in the future, this
   *  may cause a dialog box to open to allow the user to select
   *  a format.  If the argument is null, then the description goes
   *  to the clipboard.  Otherwise, it goes to the specified file.
   *  To send it to standard output, use
   *  <code>System.out</code> as an argument.
   *  @param file A file writer to which to send the description.
   */
  public void export(OutputStream out) {
    EPSGraphics g = new EPSGraphics(out, _width, _height);
    _drawPlot(g, false);
    g.showpage();
  }

  /** Rescale so that the data that is currently plotted just fits.
   *  This is done based on the public variables _xBottom, _xTop,
   *  _yBottom, and _yTop.  It is up to derived classes to ensure that
   *  variables are valid.
   *  This method calls repaint(), which eventually causes the display
   *  to be updated.
   */
  public synchronized void fillPlot() {
    _setXRange(_xBottom, _xTop);
    _setYRange(_yBottom, _yTop);
    repaint();
    // Reacquire the focus so that key bindings work.
    //requestFocus();
  }

  /** Return whether the plot uses color.
   *  @return True if the plot uses color.
   */
  public boolean getColor() {
    return _usecolor;
  }

  /** Convert a color name into a Color. Currently, only a very limited
   *  set of color names is supported: black, white, red, green, and blue.
   *  @param name A color name, or null if not found.
   *  @return An instance of Color.
   */
  public static Color getColorByName(String name) {
    try {
      // Check to see if it is a hexadecimal
      if (name.startsWith("#")) {
        name = name.substring(1);
      }
      Color col = new Color(Integer.parseInt(name, 16));
      return col;
    } catch (NumberFormatException e) {
    }
    // FIXME: This is a poor excuse for a list of colors and values.
    // We should use a hash table here.
    // Note that Color decode() wants the values to start with 0x.
    String names[][] = {
      {"black", "00000"}, {"white", "ffffff"},
      {"red", "ff0000"}, {"green", "00ff00"}, {"blue", "0000ff"}
    };
    for (int i = 0; i < names.length; i++) {
      if (name.equals(names[i][0])) {
        try {
          Color col = new Color(Integer.parseInt(names[i][1], 16));
          return col;
        } catch (NumberFormatException e) {
        }
      }
    }
    return null;
  }

  /** Get the file specification that was given by setDataurl.
   *  This method is deprecated.  Use read() instead.
   *  @deprecated
   */
  public String getDataurl() {
    return _filespec;
  }

  /** Get the document base that was set by setDocumentBase.
   *  This method is deprecated.  Use read() instead.
   *  @deprecated
   */
  public URL getDocumentBase() {
    return _documentBase;
  }

  /** Return whether the grid is drawn.
   *  @return True if a grid is drawn.
   */
  public boolean getGrid() {
    return _grid;
  }

  /** Get the legend for a dataset, or null if there is none.
   *  The legend would have been set by addLegend().
   *  @param dataset The dataset index.
   *  @return The legend label, or null if there is none.
   */
  public String getLegend(int dataset) {
    int idx = _legendDatasets.indexOf(new Integer(dataset), 0);
    if (idx != -1) {
      return (String) _legendStrings.elementAt(idx);
    } else {
      return null;
    }
  }

  /** Get the minimum size of this component.
   *  This is simply the dimensions specified by setBounds() or setSize().
   *  @return The minimum size.
   */
  public Dimension getMinimumSize() {
    return new Dimension(_width, _height);
  }


  /** Get the preferred size of this component.
   *  This is simply the dimensions specified by setBounds() or setSize().
   *  @return The preferred size.
   */
  public Dimension getPreferredSize() {
    return new Dimension(_width, _height);
  }

  /** Get the title of the graph, or an empty string if there is none.
   *  @return The title.
   */
  public String getTitle() {
    if (_title == null) return "";
    return _title;
  }

  /** Get the label for the X (horizontal) axis, or null if none has
   *  been set.
   *  @return The X label.
   */
  public String getXLabel() {
    return _xlabel;
  }

  /** Return whether the X axis is drawn with a logarithmic scale.
   *  @return True if the X axis is logarithmic.
   */
  public boolean getXLog() {
    return _xlog;
  }

  /** Get the X range.  The returned value is an array where the first
   *  element is the minimum and the second element is the maximum.
   *  return The current X range.
   */
  public double[] getXRange() {
    double[] result = new double[2];
    if (_xRangeGiven) {
      result[0] = _xlowgiven;
      result[1] = _xhighgiven;
    } else {
      // Have to first correct for the padding.
      result[0] = _xMin + ((_xMax - _xMin) * _PADDING);
      result[1] = _xMax - ((_xMax - _xMin) * _PADDING);
      ;
    }
    return result;
  }

  /** Get the X ticks that have been specified, or null if none.
   *  The return value is an array with two vectors, the first of
   *  which specifies the X tick locations (as instances of Double),
   *  and the second of which specifies the corresponding labels.
   *  @return The X ticks.
   */
  public Vector[] getXTicks() {
    if (_xticks == null) return null;
    Vector[] result = new Vector[2];
    result[0] = _xticks;
    result[1] = _xticklabels;
    return result;
  }

  /** Get the label for the Y (vertical) axis, or null if none has
   *  been set.
   *  @return The Y label.
   */
  public String getYLabel() {
    return _ylabel;
  }

  /** Return whether the Y axis is drawn with a logarithmic scale.
   *  @return True if the Y axis is logarithmic.
   */
  public boolean getYLog() {
    return _ylog;
  }

  /** Get the Y range.  The returned value is an array where the first
   *  element is the minimum and the second element is the maximum.
   *  return The current Y range.
   */
  public double[] getYRange() {
    double[] result = new double[2];
    if (_yRangeGiven) {
      result[0] = _ylowgiven;
      result[1] = _yhighgiven;
    } else {
      // Have to first correct for the padding.
      result[0] = _yMin + ((_yMax - _yMin) * _PADDING);
      result[1] = _yMax - ((_yMax - _yMin) * _PADDING);
      ;
    }
    return result;
  }

  /** Get the Y ticks that have been specified, or null if none.
   *  The return value is an array with two vectors, the first of
   *  which specifies the Y tick locations (as instances of Double),
   *  and the second of which specifies the corresponding labels.
   *  @return The Y ticks.
   */
  public Vector[] getYTicks() {
    if (_yticks == null) return null;
    Vector[] result = new Vector[2];
    result[0] = _yticks;
    result[1] = _yticklabels;
    return result;
  }

  /** Initialize the component, creating the fill button and parsing
   *  an input file, if one has been specified.  This is deprecated.
   *  Call setButtons() and read() instead.
   *  @deprecated
   */
  public void init() {
    setButtons(true);

    if (_filespec != null) {
      parseFile(_filespec, _documentBase);
    }
  }

  /** Paint the component contents, which in this base class is
   *  only the axes.
   *  @param graphics The graphics context.
   */
  public void paint(Graphics graphics) {
    // Return the if size has not been set.
    if (_buffer == null) _buffer = getGraphicsConfiguration().
            createCompatibleImage(_width, _height);

    // Double buffer for maximally smooth rendering.
    Graphics2D gBuffer = _buffer.createGraphics();


    gBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);


    _drawPlot(gBuffer, true);
    graphics.drawImage(_buffer, 0, 0, null);
    // Acquire the focus so that key bindings work.
    //requestFocus();
  }

  /** Syntactic sugar for parseFile(filespec, documentBase).
   *  This method is deprecated.  Use read() to read the old file
   *  format, or use one of the classes in the plotml package to
   *  read the XML-based file format.
   *  @deprecated
   */
  public void parseFile(String filespec) {
    parseFile(filespec, (URL) null);
  }

  /** Open up the input file, which could be stdin, a URL, or a file.
   *  @deprecated This method is deprecated.  Use read() instead.
   */
  public void parseFile(String filespec, URL documentBase) {
    DataInputStream in = null;
    if (filespec == null || filespec.length() == 0) {
      // Open up stdin
      in = new DataInputStream(System.in);
    } else {
      try {
        URL url = null;
        if (documentBase == null && _documentBase != null) {
          documentBase = _documentBase;
        }
        if (documentBase == null) {
          url = new URL(filespec);
        } else {
          try {
            url = new URL(documentBase, filespec);
          } catch (NullPointerException e) {
            // If we got a NullPointerException, then perhaps we
            // are calling this as an application, not as an applet
            url = new URL(filespec);
          }
        }
        in = new DataInputStream(url.openStream());
      } catch (MalformedURLException e) {
        try {
          // Just try to open it as a file.
          in = new DataInputStream(new FileInputStream(filespec));
        } catch (FileNotFoundException me) {
          _errorMsg = new String[2];
          _errorMsg[0] = "File not found: " + filespec;
          _errorMsg[1] = me.getMessage();
          return;
        } catch (SecurityException me) {
          _errorMsg = new String[2];
          _errorMsg[0] = "Security Exception: " + filespec;
          _errorMsg[1] = me.getMessage();
          return;
        }
      } catch (IOException ioe) {
        _errorMsg = new String[3];
        _errorMsg[0] = "Failure opening URL: ";
        _errorMsg[1] = " " + filespec;
        _errorMsg[2] = ioe.getMessage();
        return;
      }
    }

    // At this point, we've opened the data source, now read it in
    try {
      BufferedReader din = new BufferedReader(
              new InputStreamReader(in));
      String line = din.readLine();
      while (line != null) {
        _parseLine(line);
        line = din.readLine();
      }
    } catch (MalformedURLException e) {
      _errorMsg = new String[2];
      _errorMsg[0] = "Malformed URL: " + filespec;
      _errorMsg[1] = e.getMessage();
      return;
    } catch (IOException e) {
      _errorMsg = new String[2];
      _errorMsg[0] = "Failure reading data: " + filespec;
      _errorMsg[1] = e.getMessage();
      _errorMsg[1] = e.getMessage();
    } finally {
      try {
        in.close();
      } catch (IOException me) {
      }
    }
  }

  /** Read commands and/or plot data from an input stream in the old
   *  (non-XML) file syntax.
   *  To update the display, call repaint(), or make the plot visible with
   *  setVisible(true).
   *  <p>
   *  To read from standard input, use:
   *  <pre>
   *     read(System.in);
   *  </pre>
   *  To read from a url, use:
   *  <pre>
   *     read(url.openStream());
   *  </pre>
   *  To read a URL from within an applet, use:
   *  <pre>
   *     URL url = new URL(getDocumentBase(), urlSpec);
   *     read(url.openStream());
   *  </pre>
   *  Within an application, if you have an absolute URL, use:
   *  <pre>
   *     URL url = new URL(urlSpec);
   *     read(url.openStream());
   *  </pre>
   *  To read from a file, use:
   *  <pre>
   *     read(new FileInputStream(filename));
   *  </pre>
   *  @param in The input stream.
   *  @exception IOException If the stream cannot be read.
   */
  public void read(InputStream in) throws IOException {
    try {
      // NOTE: I tried to use exclusively the jdk 1.1 Reader classes,
      // but they provide no support like DataInputStream, nor
      // support for URL accesses.  So I use the older classes
      // here in a strange mixture.

      BufferedReader din = new BufferedReader(
              new InputStreamReader(in));

      try {
        String line = din.readLine();
        while (line != null) {
          _parseLine(line);
          line = din.readLine();
        }
      } finally {
        din.close();
      }
    } catch (IOException e) {
      _errorMsg = new String[2];
      _errorMsg[0] = "Failure reading input data.";
      _errorMsg[1] = e.getMessage();
      throw e;
    }
  }

  /** Read a single line command provided as a string.
   *  The commands can be any of those in the ASCII file format.
   *  @param command A command.
   */
  public void read(String command) {
    _parseLine(command);
  }

  /** Do nothing in this base class. Derived classes might want to override
   *  this class to give an example of their use.
   */
  public void samplePlot() {
    // Empty default implementation.
  }

  /** Set the background color.
   *  @param background The background color.
   */
  public void setBackground(Color background) {
    _background = background;
    super.setBackground(_background);
  }

  /** Move and resize this component. The new location of the top-left
   *  corner is specified by x and y, and the new size is specified by
   *  width and height. This overrides the base class method to make
   *  a record of the new size.
   *  @param x The new x-coordinate of this component.
   *  @param y The new y-coordinate of this component.
   *  @param width The new width of this component.
   *  @param height The new height of this component.
   */
  public void setBounds(int x, int y, int width, int height) {
    _width = width;
    _height = height;
    //_buffer = getGraphicsConfiguration().createCompatibleImage(width, height);
    _buffer = null;
    super.setBounds(x, y, _width, _height);
  }

  /** If the argument is true, make a fill button visible at the upper
   *  right.  This button auto-scales the plot.
   *  NOTE: The button may infringe on the title space,
   *  if the title is long.  In an application, it is preferable to provide
   *  a menu with the fill command.  This way, when printing the plot,
   *  the printed plot will not have a spurious button.  Thus, this method
   *  should be used only by applets, which normally do not have menus.
   */
  public void setButtons(boolean visible) {
    if (_fillButton == null) {
      _fillButton = new Button("fill");
      _fillButton.addActionListener(new FillButtonListener());
      add(_fillButton);
    }
    _fillButton.setVisible(visible);
    // Request the focus so that key events are heard.
    requestFocus();
  }

  /** If the argument is false, draw the plot without using color
   *  (in black and white).  Otherwise, draw it in color (the default).
   *  @param useColor False to draw in back and white.
   */
  public void setColor(boolean useColor) {
    _usecolor = useColor;
  }

  /** Set the file to read when init() is called.
   *  This method is deprecated.  Use read() instead.
   *  @deprecated
   */
  public void setDataurl(String filespec) {
    _filespec = filespec;
  }

  /** Set the document base to used when init() is called to read a URL.
   *  This method is deprecated.  Use read() instead.
   *  @deprecated
   */
  public void setDocumentBase(URL documentBase) {
    _documentBase = documentBase;
  }

  /** Set the foreground color.
   *  @param foreground The foreground color.
   */
  public void setForeground(Color foreground) {
    _foreground = foreground;
    super.setForeground(_foreground);
  }

  /** Control whether the grid is drawn.
   *  @param grid If true, a grid is drawn.
   */
  public void setGrid(boolean grid) {
    _grid = grid;
  }

  /** Set the label font, which is used for axis labels and legend labels.
   *  The font names understood are those understood by
   *  java.awt.Font.decode().
   *  @param name A font name.
   */
  public void setLabelFont(String name) {
    _labelFont = Font.decode(name);
    _labelFontMetrics = getFontMetrics(_labelFont);
  }

  /** Set the size of the plot.
   *  @param width The width, in pixels.
   *  @param height The height, in pixels.
   */
  public void setSize(int width, int height) {
    _width = width;
    _height = height;
    //_buffer = getGraphicsConfiguration().createCompatibleImage(width, height);
    _buffer = null;
    super.setSize(width, height);
  }

  /** Set the title of the graph.
   *  @param title The title.
   */
  public void setTitle(String title) {
    _title = title;
  }

  /** Set the title font.
   *  The font names understood are those understood by
   *  java.awt.Font.decode().
   *  @param name A font name.
   */
  public void setTitleFont(String name) {
    _titleFont = Font.decode(name);
    _titleFontMetrics = getFontMetrics(_titleFont);
  }

  /** Control whether the X axis is wrapped.
   *  If it is, then X values that are out of range are remapped
   *  to be in range using modulo arithmetic.
   *  @param wrap If true, wrapping of the X axis is enabled.
   */
  public void setWrap(boolean wrap) {
    _wrap = wrap;
  }

  /** Set the label for the X (horizontal) axis.
   *  @param label The label.
   */
  public void setXLabel(String label) {
    _xlabel = label;
  }

  /** Specify whether the X axis is drawn with a logarithmic scale.
   *  @param xlog If true, logarithmic axis is used.
   */
  public void setXLog(boolean xlog) {
    _xlog = xlog;
  }

  /** Set the X (horizontal) range of the plot.  If this is not done
   *  explicitly, then the range is computed automatically from data
   *  available when the plot is drawn.  If min and max
   *  are identical, then the range is arbitrarily spread by 1.
   *  @param min The left extent of the range.
   *  @param max The right extent of the range.
   */
  public void setXRange(double min, double max) {
    _xRangeGiven = true;
    _xlowgiven = min;
    _xhighgiven = max;
    _setXRange(min, max);
  }

  /** Set the label for the Y (vertical) axis.
   *  @param label The label.
   */
  public void setYLabel(String label) {
    _ylabel = label;
  }

  /** Specify whether the Y axis is drawn with a logarithmic scale.
   *  @param ylog If true, logarithmic axis is used.
   */
  public void setYLog(boolean ylog) {
    _ylog = ylog;
  }

  /** Set the Y (vertical) range of the plot.  If this is not done
   *  explicitly, then the range is computed automatically from data
   *  available when the plot is drawn.  If min and max are identical,
   *  then the range is arbitrarily spread by 0.1.
   *  @param min The bottom extent of the range.
   *  @param max The top extent of the range.
   */
  public void setYRange(double min, double max) {
    _yRangeGiven = true;
    _ylowgiven = min;
    _yhighgiven = max;
    _setYRange(min, max);
  }

  /** Override update so that it doesn't clear the plot.
   *  This prevents flashing of dynamic plots.
   *  Note that this means that calls to update are not passed to
   *  lightweight components.  But currently we have no lightweight
   *  components, so this is not a problem.
   */
  public void update(Graphics g) {
    paint(g);
  }

  /** Write the current data and plot configuration to the
   *  specified stream in PlotML syntax.  PlotML is an XML
   *  extension for plot data.  The written information is
   *  standalone, in that it includes the DTD (document type
   *  definition).  This makes is somewhat verbose.  To get
   *  smaller files, use the two argument version of write().
   *  The output is buffered, and is flushed and
   *  closed before exiting.  Derived classes should override _write()
   *  rather than this method.
   *  @param out An output stream.
   */
  public void write(OutputStream out) {
    write(out, null);
  }

  /** Write the current data and plot configuration to the
   *  specified stream in PlotML syntax.  PlotML is an XML
   *  extension for plot data.  The written information is not
   *  standalone, in that it refers to an external DTD (document type
   *  definition). The URL (relative or absolute) for the DTD is
   *  given as the second argument.  If that argument is null,
   *  then standalone file is written, which includes the DTD.
   *  The output is buffered, and is flushed and
   *  closed before exiting.  Derived classes should override _write()
   *  rather than this method.
   *  @param out An output stream.
   *  @param dtd The reference (URL) for the DTD.
   */
  public void write(OutputStream out, String dtd) {
    // Auto-flush is disabled.
    PrintWriter output = new PrintWriter(new BufferedOutputStream(out),
                                         false);
    if (dtd == null) {
      output.println("<?xml version=\"1.0\" standalone=\"yes\"?>");
      output.println("<!DOCTYPE plot [");
      output.println(_DTD);
      output.println("]>");
    } else {
      output.println("<?xml version=\"1.0\" standalone=\"no\"?>");
      output.println("<!DOCTYPE plot SYSTEM \"" + dtd + "\">");
    }
    output.println("<plot>");
    output.println("<!-- Ptolemy plot, version 3.1, PlotML format. -->");
    _write(output);
    output.println("</plot>");
    output.flush();
    // Avoid closing standard out.
    if (out != System.out) {
      output.close();
    }
  }

  /** Write the current data and plot configuration to the
   *  specified stream in the old PtPlot syntax.
   *  The output is buffered, and is flushed and
   *  closed before exiting.  Derived classes should override
   *  _writeOldSyntax() rather than this method.
   *  @param out An output stream.
   *  @deprecated
   */
  public void writeOldSyntax(OutputStream out) {
    // Auto-flush is disabled.
    PrintWriter output = new PrintWriter(new BufferedOutputStream(out),
                                         false);
    _writeOldSyntax(output);
    output.flush();
    // Avoid closing standard out.
    if (out != System.out) {
      output.close();
    }
  }

  /** Zoom in or out to the specified rectangle.
   *  This method calls repaint().
   *  @param lowx The low end of the new X range.
   *  @param lowy The low end of the new Y range.
   *  @param highx The high end of the new X range.
   *  @param highy The high end of the new Y range.
   */
  public synchronized void zoom(double lowx, double lowy,
                                double highx, double highy) {
    _setXRange(lowx, highx);
    _setYRange(lowy, highy);
    repaint();
  }

  ///////////////////////////////////////////////////////////////////
  ////                         public methods                 ////

  /** Draw the axes using the current range, label, and title information.
   *  If the second argument is true, clear the display before redrawing.
   *  This method is called by paint().  To cause it to be called you
   *  would normally call repaint(), which eventually causes paint() to
   *  be called.
   *  @param graphics The graphics context.
   *  @param clearfirst If true, clear the plot before proceeding.
   */
  public synchronized void _drawPlot(Graphics graphics,
                                        boolean clearfirst) {
    if (graphics == null) {
      throw new RuntimeException("PlotBox._drawPlot: Attempt to draw " +
                                 "axes without a Graphics object.");
    }
    // Find the width and height of the total drawing area, and clear it.
    Rectangle drawRect = getBounds();
    graphics.setPaintMode();
    if (clearfirst) {
      graphics.setColor(super.getBackground());
      graphics.fillRect(0, 0, drawRect.width, drawRect.height);
      graphics.setColor(Color.black);
    }

    // If an error message has been set, display it and return.
    if (_errorMsg != null) {
      int fheight = _labelFontMetrics.getHeight() + 2;
      int msgy = fheight;
      graphics.setColor(Color.black);
      for (int i = 0; i < _errorMsg.length; i++) {
        graphics.drawString(_errorMsg[i], 10, msgy);
        msgy += fheight;
        System.err.println(_errorMsg[i]);
      }
      return;
    }

    // Make sure we have an x and y range
    if (!_xRangeGiven) {
      if (_xBottom > _xTop) {
        // have nothing to go on.
        _setXRange(0, 0);
      } else {
        _setXRange(_xBottom, _xTop);
      }
    }
    if (!_yRangeGiven) {
      if (_yBottom > _yTop) {
        // have nothing to go on.
        _setYRange(0, 0);
      } else {
        _setYRange(_yBottom, _yTop);
      }
    }

    // Vertical space for title, if appropriate.
    // NOTE: We assume a one-line title.
    int titley = 0;
    int titlefontheight = _titleFontMetrics.getHeight();

    if (_title == null) {
      // FIXME: If the _title is null, then set it to the empty
      // string to solve the problem where the fill button overlaps
      // the legend if there is no title.  The fix here would
      // be to modify the legend printing text so that it takes
      // into account the case where there is no title by offsetting
      // just enough for the button.
      _title = "";
    }
    if (_title != null || _yExp != 0) {
      titley = titlefontheight + _topPadding;
    }

    // Number of vertical tick marks depends on the height of the font
    // for labeling ticks and the height of the window.
    graphics.setFont(_labelFont);
    int labelheight = _labelFontMetrics.getHeight();
    int halflabelheight = labelheight / 2;

    // Draw scaling annotation for x axis.
    // NOTE: 5 pixel padding on bottom.
    int ySPos = drawRect.height - 5;
    int xSPos = drawRect.width - _rightPadding;
    if (_xlog)
      _xExp = (int) Math.floor(_xtickMin);
    if (_xExp != 0 && _xticks == null) {
      String superscript = Integer.toString(_xExp);
      xSPos -= _superscriptFontMetrics.stringWidth(superscript);
      graphics.setFont(_superscriptFont);
      if (!_xlog) {
        graphics.drawString(superscript, xSPos,
                            ySPos - halflabelheight);
        xSPos -= _labelFontMetrics.stringWidth("x10");
        graphics.setFont(_labelFont);
        graphics.drawString("x10", xSPos, ySPos);
      }
      // NOTE: 5 pixel padding on bottom
      _bottomPadding = (3 * labelheight) / 2 + 5;
    }

    // NOTE: 5 pixel padding on the bottom.
    if (_xlabel != null && _bottomPadding < labelheight + 5) {
      _bottomPadding = labelheight + 5;
    }

    // Compute the space needed around the plot, starting with vertical.
    // NOTE: padding of 5 pixels below title.
    _uly = titley + 5;
    // NOTE: 3 pixels above bottom labels.
    _lry = drawRect.height - labelheight - _bottomPadding - 3;
    int height = _lry - _uly;
    _yscale = height / (_yMax - _yMin);
    _ytickscale = height / (_ytickMax - _ytickMin);

    ////////////////// vertical axis

    // Number of y tick marks.
    // NOTE: subjective spacing factor.
    int ny = 2 + height / (labelheight + 10);
    // Compute y increment.
    double yStep = _roundUp((_ytickMax - _ytickMin) / (double) ny);

    // Compute y starting point so it is a multiple of yStep.
    double yStart = yStep * Math.ceil(_ytickMin / yStep);

    // NOTE: Following disables first tick.  Not a good idea?
    // if (yStart == _ytickMin) yStart += yStep;

    // Define the strings that will label the y axis.
    // Meanwhile, find the width of the widest label.
    // The labels are quantized so that they don't have excess resolution.
    int widesty = 0;

    // These do not get used unless ticks are automatic, but the
    // compiler is not smart enough to allow us to reference them
    // in two distinct conditional clauses unless they are
    // allocated outside the clauses.
    String ylabels[] = new String[ny];
    int ylabwidth[] = new int[ny];

    int ind = 0;
    if (_yticks == null) {
      Vector ygrid = null;
      if (_ylog) {
        ygrid = _gridInit(yStart, yStep, true, null);
      }

      // automatic ticks
      // First, figure out how many digits after the decimal point
      // will be used.
      int numfracdigits = _numFracDigits(yStep);

      // NOTE: Test cases kept in case they are needed again.
      // System.out.println("0.1 with 3 digits: " + _formatNum(0.1, 3));
      // System.out.println("0.0995 with 3 digits: " +
      //                    _formatNum(0.0995, 3));
      // System.out.println("0.9995 with 3 digits: " +
      //                    _formatNum(0.9995, 3));
      // System.out.println("1.9995 with 0 digits: " +
      //                    _formatNum(1.9995, 0));
      // System.out.println("1 with 3 digits: " + _formatNum(1, 3));
      // System.out.println("10 with 0 digits: " + _formatNum(10, 0));
      // System.out.println("997 with 3 digits: " + _formatNum(997, 3));
      // System.out.println("0.005 needs: " + _numFracDigits(0.005));
      // System.out.println("1 needs: " + _numFracDigits(1));
      // System.out.println("999 needs: " + _numFracDigits(999));
      // System.out.println("999.0001 needs: "+_numFracDigits(999.0001));
      // System.out.println("0.005 integer digits: " +
      //                    _numIntDigits(0.005));
      // System.out.println("1 integer digits: " + _numIntDigits(1));
      // System.out.println("999 integer digits: " + _numIntDigits(999));
      // System.out.println("-999.0001 integer digits: " +
      //                    _numIntDigits(999.0001));

      double yTmpStart = yStart;
      if (_ylog)
        yTmpStart = _gridStep(ygrid, yStart, yStep, _ylog);

      for (double ypos = yTmpStart; ypos <= _ytickMax;
           ypos = _gridStep(ygrid, ypos, yStep, _ylog)) {
        // Prevent out of bounds exceptions
        if (ind >= ny) break;
        String yticklabel;
        if (_ylog) {
          yticklabel = _formatLogNum(ypos, numfracdigits);
        } else {
          yticklabel = _formatNum(ypos, numfracdigits);
        }
        ylabels[ind] = yticklabel;
        int lw = _labelFontMetrics.stringWidth(yticklabel);
        ylabwidth[ind++] = lw;
        if (lw > widesty) {
          widesty = lw;
        }
      }
    } else {
      // explicitly specified ticks
      Enumeration nl = _yticklabels.elements();
      while (nl.hasMoreElements()) {
        String label = (String) nl.nextElement();
        int lw = _labelFontMetrics.stringWidth(label);
        if (lw > widesty) {
          widesty = lw;
        }
      }
    }

    // Next we do the horizontal spacing.
    if (_ylabel != null) {
      _ulx = widesty + _labelFontMetrics.stringWidth("W") + _leftPadding;
    } else {
      _ulx = widesty + _leftPadding;
    }
    int legendwidth = _drawLegend(graphics,
                                  drawRect.width - _rightPadding, _uly);
    _lrx = drawRect.width - legendwidth - _rightPadding;
    int width = _lrx - _ulx;
    _xscale = width / (_xMax - _xMin);
    _xtickscale = width / (_xtickMax - _xtickMin);

    // Background for the plotting rectangle.
    // Always use a white background because the dataset colors
    // were designed for a white background.
    graphics.setColor(Color.white);
    graphics.fillRect(_ulx, _uly, width, height);

    graphics.setColor(_foreground);
    graphics.drawRect(_ulx, _uly, width, height);

    // NOTE: subjective tick length.
    int tickLength = 5;
    int xCoord1 = _ulx + tickLength;
    int xCoord2 = _lrx - tickLength;

    if (_yticks == null) {
      // auto-ticks
      Vector ygrid = null;
      double yTmpStart = yStart;
      if (_ylog) {
        ygrid = _gridInit(yStart, yStep, true, null);
        yTmpStart = _gridStep(ygrid, yStart, yStep, _ylog);
        ny = ind;
      }
      ind = 0;
      // Set to false if we don't need the exponent
      boolean needExponent = _ylog;
      for (double ypos = yTmpStart; ypos <= _ytickMax;
           ypos = _gridStep(ygrid, ypos, yStep, _ylog)) {
        // Prevent out of bounds exceptions
        if (ind >= ny) break;
        int yCoord1 = _lry - (int) ((ypos - _ytickMin) * _ytickscale);
        // The lowest label is shifted up slightly to avoid
        // colliding with x labels.
        int offset = 0;
        if (ind > 0 && !_ylog) offset = halflabelheight;
        graphics.drawLine(_ulx, yCoord1, xCoord1, yCoord1);
        graphics.drawLine(_lrx, yCoord1, xCoord2, yCoord1);
        if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
          graphics.setColor(Color.lightGray);
          graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
          graphics.setColor(_foreground);
        }
        // Check to see if any of the labels printed contain
        // the exponent.  If we don't see an exponent, then print it.
        if (_ylog && ylabels[ind].indexOf('e') != -1)
          needExponent = false;

        // NOTE: 4 pixel spacing between axis and labels.
        graphics.drawString(ylabels[ind],
                            _ulx - ylabwidth[ind++] - 4, yCoord1 + offset);
      }

      if (_ylog) {
        // Draw in grid lines that don't have labels.
        Vector unlabeledgrid = _gridInit(yStart, yStep, false, ygrid);
        if (unlabeledgrid.size() > 0) {
          // If the step is greater than 1, clamp it to 1 so that
          // we draw the unlabeled grid lines for each
          //integer interval.
          double tmpStep = (yStep > 1.0)? 1.0 : yStep;

          for (double ypos = _gridStep(unlabeledgrid, yStart,
                                       tmpStep, _ylog);
               ypos <= _ytickMax;
               ypos = _gridStep(unlabeledgrid, ypos,
                                tmpStep, _ylog)) {
            int yCoord1 = _lry -
                    (int) ((ypos - _ytickMin) * _ytickscale);
            if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
              graphics.setColor(Color.lightGray);
              graphics.drawLine(_ulx + 1, yCoord1,
                                _lrx - 1, yCoord1);
              graphics.setColor(_foreground);
            }
          }
        }

        if (needExponent) {
          // We zoomed in, so we need the exponent
          _yExp = (int) Math.floor(yTmpStart);
        } else {
          _yExp = 0;
        }
      }

      // Draw scaling annotation for y axis.
      if (_yExp != 0) {
        graphics.drawString("x10", 2, titley);
        graphics.setFont(_superscriptFont);
        graphics.drawString(Integer.toString(_yExp),
                            _labelFontMetrics.stringWidth("x10") + 2,
                            titley - halflabelheight);
        graphics.setFont(_labelFont);
      }
    } else {
      // ticks have been explicitly specified
      Enumeration nt = _yticks.elements();
      Enumeration nl = _yticklabels.elements();

      while (nl.hasMoreElements()) {
        String label = (String) nl.nextElement();
        double ypos = ((Double) (nt.nextElement())).doubleValue();
        if (ypos > _yMax || ypos < _yMin) continue;
        int yCoord1 = _lry - (int) ((ypos - _yMin) * _yscale);
        int offset = 0;
        if (ypos < _lry - labelheight) offset = halflabelheight;
        graphics.drawLine(_ulx, yCoord1, xCoord1, yCoord1);
        graphics.drawLine(_lrx, yCoord1, xCoord2, yCoord1);
        if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
          graphics.setColor(Color.lightGray);
          graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
          graphics.setColor(_foreground);
        }
        // NOTE: 3 pixel spacing between axis and labels.
        graphics.drawString(label,
                            _ulx - _labelFontMetrics.stringWidth(label) - 3,
                            yCoord1 + offset);
      }
    }


    //////////////////// horizontal axis
    int yCoord1 = _uly + tickLength;
    int yCoord2 = _lry - tickLength;
    if (_xticks == null) {
      // auto-ticks

      // Number of x tick marks.
      // Need to start with a guess and converge on a solution here.
      int nx = 10;
      double xStep = 0.0;
      int numfracdigits = 0;
      int charwidth = _labelFontMetrics.stringWidth("8");
      if (_xlog) {
        // X axes log labels will be at most 6 chars: -1E-02
        nx = 2 + width / ((charwidth * 6) + 10);
      } else {
        // Limit to 10 iterations
        int count = 0;
        while (count++ <= 10) {
          xStep = _roundUp((_xtickMax - _xtickMin) / (double) nx);
          // Compute the width of a label for this xStep
          numfracdigits = _numFracDigits(xStep);
          // Number of integer digits is the maximum of two endpoints
          int intdigits = _numIntDigits(_xtickMax);
          int inttemp = _numIntDigits(_xtickMin);
          if (intdigits < inttemp) {
            intdigits = inttemp;
          }
          // Allow two extra digits (decimal point and sign).
          int maxlabelwidth = charwidth *
                  (numfracdigits + 2 + intdigits);
          // Compute new estimate of number of ticks.
          int savenx = nx;
          // NOTE: 10 additional pixels between labels.
          // NOTE: Try to ensure at least two tick marks.
          nx = 2 + width / (maxlabelwidth + 10);
          if (nx - savenx <= 1 || savenx - nx <= 1) break;
        }
      }
      xStep = _roundUp((_xtickMax - _xtickMin) / (double) nx);
      numfracdigits = _numFracDigits(xStep);

      // Compute x starting point so it is a multiple of xStep.
      double xStart = xStep * Math.ceil(_xtickMin / xStep);

      // NOTE: Following disables first tick.  Not a good idea?
      // if (xStart == _xMin) xStart += xStep;

      Vector xgrid = null;
      double xTmpStart = xStart;
      if (_xlog) {
        xgrid = _gridInit(xStart, xStep, true, null);
        //xgrid = _gridInit(xStart, xStep);
        xTmpStart = _gridRoundUp(xgrid, xStart);
      }

      // Set to false if we don't need the exponent
      boolean needExponent = _xlog;

      // Label the x axis.  The labels are quantized so that
      // they don't have excess resolution.
      for (double xpos = xTmpStart;
           xpos <= _xtickMax;
           xpos = _gridStep(xgrid, xpos, xStep, _xlog)) {
        String xticklabel;
        if (_xlog) {
          xticklabel = _formatLogNum(xpos, numfracdigits);
          if (xticklabel.indexOf('e') != -1)
            needExponent = false;
        } else {
          xticklabel = _formatNum(xpos, numfracdigits);
        }
        xCoord1 = _ulx + (int) ((xpos - _xtickMin) * _xtickscale);
        graphics.drawLine(xCoord1, _uly, xCoord1, yCoord1);
        graphics.drawLine(xCoord1, _lry, xCoord1, yCoord2);
        if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
          graphics.setColor(Color.lightGray);
          graphics.drawLine(xCoord1, yCoord1, xCoord1, yCoord2);
          graphics.setColor(_foreground);
        }
        int labxpos = xCoord1 -
                _labelFontMetrics.stringWidth(xticklabel) / 2;
        // NOTE: 3 pixel spacing between axis and labels.
        graphics.drawString(xticklabel, labxpos,
                            _lry + 3 + labelheight);
      }

      if (_xlog) {
        // Draw in grid lines that don't have labels.

        // If the step is greater than 1, clamp it to 1 so that
        // we draw the unlabeled grid lines for each
        // integer interval.
        double tmpStep = (xStep > 1.0)? 1.0 : xStep;

        // Recalculate the start using the new step.
        xTmpStart = tmpStep * Math.ceil(_xtickMin / tmpStep);

        Vector unlabeledgrid = _gridInit(xTmpStart, tmpStep,
                                         false, xgrid);
        if (unlabeledgrid.size() > 0) {
          for (double xpos = _gridStep(unlabeledgrid, xTmpStart,
                                       tmpStep, _xlog);
               xpos <= _xtickMax;
               xpos = _gridStep(unlabeledgrid, xpos,
                                tmpStep, _xlog)) {
            xCoord1 = _ulx + (int) ((xpos - _xtickMin) * _xtickscale);
            if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
              graphics.setColor(Color.lightGray);
              graphics.drawLine(xCoord1, _uly + 1,
                                xCoord1, _lry - 1);
              graphics.setColor(_foreground);
            }
          }
        }

        if (needExponent) {
          _xExp = (int) Math.floor(xTmpStart);
          graphics.setFont(_superscriptFont);
          graphics.drawString(Integer.toString(_xExp), xSPos,
                              ySPos - halflabelheight);
          xSPos -= _labelFontMetrics.stringWidth("x10");
          graphics.setFont(_labelFont);
          graphics.drawString("x10", xSPos, ySPos);
        } else {
          _xExp = 0;
        }
      }


    } else {
      // ticks have been explicitly specified
      Enumeration nt = _xticks.elements();
      Enumeration nl = _xticklabels.elements();
      // Code contributed by Jun Wu (jwu@inin.com.au)
      double preLength = 0.0;
      while (nl.hasMoreElements()) {
        String label = (String) nl.nextElement();
        double xpos = ((Double) (nt.nextElement())).doubleValue();
        // If xpos is out of range, ignore.
        if (xpos > _xMax || xpos < _xMin) continue;

        // Find the center position of the label.
        xCoord1 = _ulx + (int) ((xpos - _xMin) * _xscale);

        // Find  the start position of x label.
        int labxpos = xCoord1 - _labelFontMetrics.stringWidth(label) / 2;

        // If the labels are not overlapped, proceed.
        if (labxpos > preLength) {
          // calculate the length of the label
          preLength = xCoord1
                  + _labelFontMetrics.stringWidth(label) / 2 + 10;

          // Draw the label.
          // NOTE: 3 pixel spacing between axis and labels.
          graphics.drawString(label,
                              labxpos, _lry + 3 + labelheight);

          // Draw the label mark on the axis
          graphics.drawLine(xCoord1, _uly, xCoord1, yCoord1);
          graphics.drawLine(xCoord1, _lry, xCoord1, yCoord2);

          // Draw the grid line
          if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
            graphics.setColor(Color.lightGray);
            graphics.drawLine(xCoord1, yCoord1,
                              xCoord1, yCoord2);
            graphics.setColor(_foreground);
          }
        }
      }
    }

    //////////////////// Draw title and axis labels now.

    // Center the title and X label over the plotting region, not
    // the window.
    graphics.setColor(_foreground);

    if (_title != null) {
      graphics.setFont(_titleFont);
      int titlex = _ulx +
              (width - _titleFontMetrics.stringWidth(_title)) / 2;
      graphics.drawString(_title, titlex, titley);
    }

    graphics.setFont(_labelFont);
    if (_xlabel != null) {
      int labelx = _ulx +
              (width - _labelFontMetrics.stringWidth(_xlabel)) / 2;
      graphics.drawString(_xlabel, labelx, ySPos);
    }

    int charcenter = 2 + _labelFontMetrics.stringWidth("W") / 2;
    int charheight = labelheight;
    if (_ylabel != null) {
      // Vertical label is fairly complex to draw.
      int yl = _ylabel.length();
      int starty = _uly + (_lry - _uly) / 2 - yl * charheight / 2 + charheight;
      for (int i = 0; i < yl; i++) {
        String nchar = _ylabel.substring(i, i + 1);
        int cwidth = _labelFontMetrics.stringWidth(nchar);
        graphics.drawString(nchar, charcenter - cwidth / 2, starty);
        starty += charheight;
      }
    }
  }

  /** Put a mark corresponding to the specified dataset at the
   *  specified x and y position.   The mark is drawn in the
   *  current color.  In this base class, a point is a
   *  filled rectangle 6 pixels across.  Note that marks greater than
   *  about 6 pixels in size will not look very good since they will
   *  overlap axis labels and may not fit well in the legend.   The
   *  <i>clip</i> argument, if <code>true</code>, states
   *  that the point should not be drawn if
   *  it is out of range.
   *  @param graphics The graphics context.
   *  @param dataset The index of the data set.
   *  @param xpos The X position.
   *  @param ypos The Y position.
   *  @param clip If true, do not draw if out of range.
   */
  public void _drawPoint(Graphics graphics,
                            int dataset, long xpos, long ypos, boolean clip) {
    boolean pointinside = ypos <= _lry && ypos >= _uly &&
            xpos <= _lrx && xpos >= _ulx;
    if (!pointinside && clip) {
      return;
    }
    graphics.fillRect((int) xpos - 6, (int) ypos - 6, 6, 6);
  }

  /** Display basic information in its own window.
   */
  public void _help() {
    Message message = new Message(
            "Ptolemy plot package\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
            "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "Version 3.1, Build: $Id: RepastPlotBox.java,v 1.10 2004/11/03 19:51:02 jerryvos Exp $\n\n" +
            "Key bindings:\n" +
            // FIXME: When clipboard works. Also fix class comment.
            // "   Cntr-c:  copy plot to clipboard (EPS format)\n" +
            "   D: dump plot data to standard out\n" +
            "   E: export plot to standard out (EPS format)\n" +
            "   F: fill plot\n" +
            "   H or ?: print help message (this message)\n" +
            "   Cntr-D or Q: quit\n" +
            "For more information, see\n" +
            "http://ptolemy.eecs.berkeley.edu/java/ptplot\n",
            null, null, 24, 60, TextArea.SCROLLBARS_NONE);
    message.setTitle("About Ptolemy Plot Package");
  }

  /** Parse a line that gives plotting information.  In this base
   *  class, only lines pertaining to the title and labels are processed.
   *  Everything else is ignored. Return true if the line is recognized.
   *  @param line A line of text.
   */
  public boolean _parseLine(String line) {
    // If you modify this method, you should also modify write()

    // We convert the line to lower case so that the command
    // names are case insensitive.
    String lcLine = new String(line.toLowerCase());
    if (lcLine.startsWith("#")) {
      // comment character
      return true;
    } else if (lcLine.startsWith("titletext:")) {
      setTitle((line.substring(10)).trim());
      return true;
    } else if (lcLine.startsWith("title:")) {
      // Tolerate alternative tag.
      setTitle((line.substring(10)).trim());
      return true;
    } else if (lcLine.startsWith("xlabel:")) {
      setXLabel((line.substring(7)).trim());
      return true;
    } else if (lcLine.startsWith("ylabel:")) {
      setYLabel((line.substring(7)).trim());
      return true;
    } else if (lcLine.startsWith("xrange:")) {
      int comma = line.indexOf(",", 7);
      if (comma > 0) {
        String min = (line.substring(7, comma)).trim();
        String max = (line.substring(comma + 1)).trim();
        try {
          Double dmin = new Double(min);
          Double dmax = new Double(max);
          setXRange(dmin.doubleValue(), dmax.doubleValue());
        } catch (NumberFormatException e) {
          // ignore if format is bogus.
        }
      }
      return true;
    } else if (lcLine.startsWith("yrange:")) {
      int comma = line.indexOf(",", 7);
      if (comma > 0) {
        String min = (line.substring(7, comma)).trim();
        String max = (line.substring(comma + 1)).trim();
        try {
          Double dmin = new Double(min);
          Double dmax = new Double(max);
          setYRange(dmin.doubleValue(), dmax.doubleValue());
        } catch (NumberFormatException e) {
          // ignore if format is bogus.
        }
      }
      return true;
    } else if (lcLine.startsWith("xticks:")) {
      // example:
      // XTicks "label" 0, "label" 1, "label" 3
      _parsePairs(line.substring(7), true);
      return true;
    } else if (lcLine.startsWith("yticks:")) {
      // example:
      // YTicks "label" 0, "label" 1, "label" 3
      _parsePairs(line.substring(7), false);
      return true;
    } else if (lcLine.startsWith("xlog:")) {
      if (lcLine.indexOf("off", 5) >= 0) {
        _xlog = false;
      } else {
        _xlog = true;
      }
      return true;
    } else if (lcLine.startsWith("ylog:")) {
      if (lcLine.indexOf("off", 5) >= 0) {
        _ylog = false;
      } else {
        _ylog = true;
      }
      return true;
    } else if (lcLine.startsWith("grid:")) {
      if (lcLine.indexOf("off", 5) >= 0) {
        _grid = false;
      } else {
        _grid = true;
      }
      return true;
    } else if (lcLine.startsWith("wrap:")) {
      if (lcLine.indexOf("off", 5) >= 0) {
        _wrap = false;
      } else {
        _wrap = true;
      }
      return true;
    } else if (lcLine.startsWith("color:")) {
      if (lcLine.indexOf("off", 6) >= 0) {
        _usecolor = false;
      } else {
        _usecolor = true;
      }
      return true;
    }
    return false;
  }

  /** Set the visibility of the Fill button.
   *  This is deprecated.  Use setButtons().
   *  @deprecated
   */
  public void _setButtonsVisibility(boolean vis) {
    _fillButton.setVisible(vis);
  }

  /** Write plot information to the specified output stream in PlotML.
   *  Derived classes should override this method to first call
   *  the parent class method, then add whatever additional information
   *  they wish to add to the stream.
   *  @param output A buffered print writer.
   */
  public void _write(PrintWriter output) {
    // NOTE: If you modify this, you should change the _DTD variable
    // accordingly.
    if (_title != null)
      output.println(
              "<title>" + _title + "</title>");
    if (_xlabel != null)
      output.println(
              "<xLabel>" + _xlabel + "</xLabel>");
    if (_ylabel != null)
      output.println(
              "<yLabel>" + _ylabel + "</yLabel>");
    if (_xRangeGiven)
      output.println(
              "<xRange min=\"" + _xlowgiven + "\" max=\""
              + _xhighgiven + "\"/>");
    if (_yRangeGiven)
      output.println(
              "<yRange min=\"" + _ylowgiven + "\" max=\""
              + _yhighgiven + "\"/>");
    if (_xticks != null && _xticks.size() > 0) {
      output.println("<xTicks>");
      int last = _xticks.size() - 1;
      for (int i = 0; i <= last; i++) {
        output.println("  <tick label=\""
                       + (String) _xticklabels.elementAt(i) + "\" position=\""
                       + (Double) _xticks.elementAt(i) + "\"/>");
      }
      output.println("</xTicks>");
    }
    if (_yticks != null && _yticks.size() > 0) {
      output.println("<yTicks>");
      int last = _yticks.size() - 1;
      for (int i = 0; i <= last; i++) {
        output.println("  <tick label=\""
                       + (String) _yticklabels.elementAt(i) + "\" position=\""
                       + (Double) _yticks.elementAt(i) + "\"/>");
      }
      output.println("</yTicks>");
    }
    if (_xlog) output.println("<xLog/>");
    if (_ylog) output.println("<yLog/>");
    if (!_grid) output.println("<noGrid/>");
    if (_wrap) output.println("<wrap/>");
    if (!_usecolor) output.println("<noColor/>");
  }

  /** Write plot information to the specified output stream in the
   *  old PtPlot syntax.
   *  Derived classes should override this method to first call
   *  the parent class method, then add whatever additional information
   *  they wish to add to the stream.
   *  @param output A buffered print writer.
   *  @deprecated
   */
  public void _writeOldSyntax(PrintWriter output) {
    output.println("# Ptolemy plot, version 2.0");
    if (_title != null) output.println("TitleText: " + _title);
    if (_xlabel != null) output.println("XLabel: " + _xlabel);
    if (_ylabel != null) output.println("YLabel: " + _ylabel);
    if (_xRangeGiven)
      output.println("XRange: " + _xlowgiven
                     + ", " + _xhighgiven);
    if (_yRangeGiven)
      output.println("YRange: " + _ylowgiven
                     + ", " + _yhighgiven);
    if (_xticks != null && _xticks.size() > 0) {
      output.print("XTicks: ");
      int last = _xticks.size() - 1;
      for (int i = 0; i < last; i++) {
        output.print("\"" + (String) _xticklabels.elementAt(i) + "\" "
                     + (Double) _xticks.elementAt(i) + ", ");
      }
      output.println("\"" + (String) _xticklabels.elementAt(last) + "\" "
                     + (Double) _xticks.elementAt(last));
    }
    if (_yticks != null && _yticks.size() > 0) {
      output.print("YTicks: ");
      int last = _yticks.size() - 1;
      for (int i = 0; i < last; i++) {
        output.print("\"" + (String) _yticklabels.elementAt(i) + "\" "
                     + (Double) _yticks.elementAt(i) + ", ");
      }
      output.println("\"" + (String) _yticklabels.elementAt(last) + "\" "
                     + (Double) _yticks.elementAt(last));
    }
    if (_xlog) output.println("XLog: on");
    if (_ylog) output.println("YLog: on");
    if (!_grid) output.println("Grid: off");
    if (_wrap) output.println("Wrap: on");
    if (!_usecolor) output.println("Color: off");
  }

  ///////////////////////////////////////////////////////////////////
  ////                         public variables               ////

  // The range of the data to be plotted.
  public transient double _yMax = 0, _yMin = 0, _xMax = 0, _xMin = 0;

  // The factor we pad by so that we don't plot points on the axes.
  public transient static final double _PADDING = 0.05;

  // Whether the ranges have been given.
  public transient boolean _xRangeGiven = false;
  public transient boolean _yRangeGiven = false;
  public transient boolean _rangesGivenByZooming = false;

  /** @serial The given X and Y ranges.
   * If they have been given the top and bottom of the x and y ranges.
   * This is different from _xMin and _xMax, which actually represent
   * the range of data that is plotted.  This represents the range
   * specified (which may be different due to zooming).
   */
  public double _xlowgiven, _xhighgiven, _ylowgiven, _yhighgiven;

  /** @serial The minimum X value registered so for, for auto ranging. */
  public double _xBottom = Double.MAX_VALUE;

  /** @serial The maximum X value registered so for, for auto ranging. */
  public double _xTop = -Double.MAX_VALUE;

  /** @serial The minimum Y value registered so for, for auto ranging. */
  public double _yBottom = Double.MAX_VALUE;

  /** @serial The maximum Y value registered so for, for auto ranging. */
  public double _yTop = -Double.MAX_VALUE;

  /** @serial Whether to draw the axes using a logarithmic scale. */
  public boolean _xlog = false, _ylog = false;

  // For use in calculating log base 10. A log times this is a log base 10.
  public static final double _LOG10SCALE = 1 / Math.log(10);

  /** @serial Whether to draw a background grid. */
  public boolean _grid = true;

  /** @serial Whether to wrap the X axis. */
  public boolean _wrap = false;

  /** @serial Color of the background, settable from HTML. */
  public Color _background = Color.white;

  /** @serial Color of the foreground, settable from HTML. */
  public Color _foreground = Color.black;

  /** @serial Top padding.
   *  Derived classes can increment these to make space around the plot.
   */
  public int _topPadding = 10;

  /** @serial Bottom padding.
   *  Derived classes can increment these to make space around the plot.
   */
  public int _bottomPadding = 5;

  /** @serial Right padding.
   *  Derived classes can increment these to make space around the plot.
   */
  public int _rightPadding = 10;

  /** @serial Left padding.
   *  Derived classes can increment these to make space around the plot.
   */
  public int _leftPadding = 10;

  /** @serial The plot rectangle in pixels.
   * The naming convention is: "_ulx" = "upper left x", where "x" is
   * the horizontal dimension.
   */
  public int _ulx = 1 , _uly = 1, _lrx = 100, _lry = 100;

  /** @serial Scaling used in plotting points. */
  public double _yscale = 1.0, _xscale = 1.0;

  /** @serial Indicator whether to use _colors. */
  public boolean _usecolor = true;

  // Default _colors, by data set.
  // There are 11 colors so that combined with the
  // 10 marks of the Plot class, we can distinguish 110
  // distinct data sets.
  static public Color[] _colors = {
    new Color(0xff0000), // red
    new Color(0x0000ff), // blue
    new Color(0x00aaaa), // cyan-ish
    new Color(0x000000), // black
    new Color(0xffa500), // orange
    new Color(0x53868b), // cadetblue4
    new Color(0xff7f50), // coral
    new Color(0x45ab1f), // dark green-ish
    new Color(0x90422d), // sienna-ish
    new Color(0xa0a0a0), // grey-ish
    new Color(0x14ff14), // green-ish
  };

  // maps colors to datasets so we can specify a color for our dataset
  // and legend
  public OpenIntObjectHashMap dsColorMap = new OpenIntObjectHashMap();

  // maps mark symbols to datasets so we can specify the marks for our
  // datasets and legends.
  public OpenIntIntHashMap dsMarkMap = new OpenIntIntHashMap();

  // Maximum number of different marks
  // NOTE: There are 11 colors in the base class.  Combined with 10
  // marks, that makes 110 unique signal identities.
  public static final int _MAX_MARKS = 10;


  /** @serial Width and height of component in pixels. */
  public int _width = 400, _height = 400;

  /** @serial The document base we use to find the _filespec.
   * NOTE: Use of this variable is deprecated.  But it is made available
   * to derived classes for backward compatibility.
   * FIXME: Sun's appletviewer gives an exception if this is public.
   * Why?? So we make it temporarily public.
   */
  public URL _documentBase = null;


  ///////////////////////////////////////////////////////////////////
  ////                         public methods                   ////

  /*
   * Draw the legend in the upper right corner and return the width
   * (in pixels)  used up.  The arguments give the upper right corner
   * of the region where the legend should be placed.
   */
  public int _drawLegend(Graphics graphics, int urx, int ury) {
    // FIXME: consolidate all these for efficiency
    graphics.setFont(_labelFont);
    int spacing = _labelFontMetrics.getHeight();

    Enumeration v = _legendStrings.elements();
    Enumeration i = _legendDatasets.elements();
    int ypos = ury + spacing;
    int maxwidth = 0;
    while (v.hasMoreElements()) {
      String legend = (String) v.nextElement();
      // NOTE: relies on _legendDatasets having the same num. of entries.
      int dataset = ((Integer) i.nextElement()).intValue();
      if (dataset >= 0) {
        if (_usecolor) {
          // Points are only distinguished up to the number of colors
          Color color = (Color) dsColorMap.get(dataset);
//int color = dataset % _colors.length;
//graphics.setColor(_colors[color]);
          graphics.setColor(color);
        }
        _drawPoint(graphics, dataset, urx - 3, ypos - 3, false);

        graphics.setColor(_foreground);
        int width = _labelFontMetrics.stringWidth(legend);
        if (width > maxwidth) maxwidth = width;
        graphics.drawString(legend, urx - 15 - width, ypos);
        ypos += spacing;
      }
    }

    int detailMax = _drawDetails(graphics, urx, ypos, spacing);
    if (detailMax > maxwidth) maxwidth = detailMax;
    return 22 + maxwidth;  // NOTE: subjective spacing parameter.
  }

  public int _drawDetails(Graphics graphics, int urx, int ury, int spacing) {
    int size = _detailsStrings.size();
    int maxwidth = 0;
    if (size > 0) {

      int ypos = ury + spacing + 5;
      for (int i = 0; i < size; i++) {
        String detail = (String) _detailsStrings.get(i);
        int width = _labelFontMetrics.stringWidth(detail);
        if (width > maxwidth) maxwidth = width;
        graphics.drawString(detail, urx - 12 - width, ypos);
        ypos += spacing;
      }

      graphics.drawRect(urx - 15 - maxwidth, ury + 5,
                        maxwidth + 6, ypos - ury - spacing);
      maxwidth += 6;
    }

    return maxwidth;
  }

  /*
   * Return the number as a String for use as a label on a
   * logarithmic axis.
   * Since this is a log plot, number passed in will not have too many
   * digits to cause problems.
   * If the number is an integer, then we print 1e<num>.
   * If the number is not an integer, then print only the fractional
   * components.
   */
  public String _formatLogNum(double num, int numfracdigits) {
    String results;
    int exponent = (int) num;

    // Determine the exponent, prepending 0 or -0 if necessary.
    if (exponent >= 0 && exponent < 10) {
      results = "0" + exponent;
    } else {
      if (exponent < 0 && exponent > -10) {
        results = "-0" + (-exponent);
      } else {
        results = Integer.toString(exponent);
      }
    }

    // Handle the mantissa.
    if (num >= 0.0) {
      if (num - (int) (num) < 0.001) {
        results = "1e" + results;
      } else {
        results = _formatNum(Math.pow(10.0, (num - (int) num)),
                             numfracdigits);
      }
    } else {
      if (-num - (int) (-num) < 0.001) {
        results = "1e" + results;
      } else {
        results = _formatNum(Math.pow(10.0, (num - (int) num)) * 10,
                             numfracdigits);
      }
    }
    return results;
  }

  /*
   * Return a string for displaying the specified number
   * using the specified number of digits after the decimal point.
   * NOTE: java.text.NumberFormat in Netscape 4.61 has a bug
   * where it fails to round numbers instead it truncates them.
   * As a result, we don't use java.text.NumberFormat, instead
   * We use the method from Ptplot3.1
   */
  public String _formatNum(double num, int numfracdigits) {
    // When java.text.NumberFormat works under Netscape,
    // uncomment the next block of code and remove
    // the code after it.
    // Ptplot developers at UCB can access a test case at:
    // http://ptolemy.eecs.berkeley.edu/~ptII/ptIItree/ptolemy/plot/adm/trunc/trunc-jdk11.html
    // The plot will show two 0.7 values on the x axis if the bug
    // continues to exist.

    //if (_numberFormat == null) {
    //   // Cache the number format so that we don't have to get
    //    // info about local language etc. from the OS each time.
    //    _numberFormat = NumberFormat.getInstance();
    //}
    //_numberFormat.setMinimumFractionDigits(numfracdigits);
    //_numberFormat.setMaximumFractionDigits(numfracdigits);
    //return _numberFormat.format(num);

    // The section below is from Ptplot3.1

    // First, round the number.
    double fudge = 0.5;
    if (num < 0.0) fudge = -0.5;
    String numString = Double.toString(num +
                                       fudge * Math.pow(10.0, -numfracdigits));
    // Next, find the decimal point.
    int dpt = numString.lastIndexOf(".");
    StringBuffer result = new StringBuffer();
    if (dpt < 0) {
      // The number we are given is an integer.
      if (numfracdigits <= 0) {
        // The desired result is an integer.
        result.append(numString);
        return result.toString();
      }
      // Append a decimal point and some zeros.
      result.append(".");
      for (int i = 0; i < numfracdigits; i++) {
        result.append("0");
      }
      return result.toString();
    } else {
      // There are two cases.  First, there may be enough digits.
      int shortby = numfracdigits - (numString.length() - dpt - 1);
      if (shortby <= 0) {
        int numtocopy = dpt + numfracdigits + 1;
        if (numfracdigits == 0) {
          // Avoid copying over a trailing decimal point.
          numtocopy -= 1;
        }
        result.append(numString.substring(0, numtocopy));
        return result.toString();
      } else {
        result.append(numString);
        for (int i = 0; i < shortby; i++) {
          result.append("0");
        }
        return result.toString();
      }
    }
  }

  /*
   * Determine what values to use for log axes.
   * Based on initGrid() from xgraph.c by David Harrison.
   */
  public Vector _gridInit(double low, double step, boolean labeled,
                           Vector oldgrid) {

    // How log axes work:
    // _gridInit() creates a vector with the values to use for the
    // log axes.  For example, the vector might contain
    // {0.0 0.301 0.698}, which could correspond to
    // axis labels {1 1.2 1.5 10 12 15 100 120 150}
    //
    // _gridStep() gets the proper value.  _gridInit is cycled through
    // for each integer log value.
    //
    // Bugs in log axes:
    // * Sometimes not enough grid lines are displayed because the
    // region is small.  This bug is present in the original xgraph
    // binary, which is the basis of this code.  The problem is that
    // as ratio gets closer to 1.0, we need to add more and more
    // grid marks.

    Vector grid = new Vector(10);
    //grid.addElement(new Double(0.0));
    double ratio = Math.pow(10.0, step);
    int ngrid = 1;
    if (labeled) {
      // Set up the number of grid lines that will be labeled
      if (ratio <= 3.5) {
        if (ratio > 2.0)
          ngrid = 2;
        else if (ratio > 1.26)
          ngrid = 5;
        else if (ratio > 1.125)
          ngrid = 10;
        else
          ngrid = (int) Math.rint(1.0 / step);

      }
    } else {
      // Set up the number of grid lines that will not be labeled
      if (ratio > 10.0)
        ngrid = 1;
      else if (ratio > 3.0)
        ngrid = 2;
      else if (ratio > 2.0)
        ngrid = 5;
      else if (ratio > 1.125)
        ngrid = 10;
      else
        ngrid = 100;
      // Note: we should keep going here, but this increases the
      // size of the grid array and slows everything down.
    }

    int oldgridi = 0;
    for (int i = 0; i < ngrid; i++) {
      double gridval = i * 1.0 / ngrid * 10;
      double logval = _LOG10SCALE * Math.log(gridval);
      if (logval == Double.NEGATIVE_INFINITY)
        logval = 0.0;

      // If oldgrid is not null, then do not draw lines that
      // were already drawn in oldgrid.  This is necessary
      // so we avoid obliterating the tick marks on the plot borders.
      if (oldgrid != null && oldgridi < oldgrid.size()) {

        // Cycle through the oldgrid until we find an element
        // that is equal to or greater than the element we are
        // trying to add.
        while (oldgridi < oldgrid.size() &&
                ((Double) oldgrid.elementAt(oldgridi)).doubleValue() <
                logval) {
          oldgridi++;
        }

        if (oldgridi < oldgrid.size()) {
          // Using == on doubles is bad if the numbers are close,
          // but not exactly equal.
          if (Math.abs(
                  ((Double) oldgrid.elementAt(oldgridi)).doubleValue()
                  - logval)
                  > 0.00001) {
            grid.addElement(new Double(logval));
          }
        } else {
          grid.addElement(new Double(logval));
        }
      } else {
        grid.addElement(new Double(logval));
      }
    }

    // _gridCurJuke and _gridBase are used in _gridStep();
    _gridCurJuke = 0;
    if (low == -0.0)
      low = 0.0;
    _gridBase = Math.floor(low);
    double x = low - _gridBase;

    // Set gridCurJuke so that the value in grid is greater than
    // or equal to x.  This sets us up to process the first point.
    for (_gridCurJuke = -1;
         (_gridCurJuke + 1) < grid.size() && x >=
            ((Double) grid.elementAt(_gridCurJuke + 1)).doubleValue();
         _gridCurJuke++) {
    }
    return grid;
  }

  /*
   * Round pos up to the nearest value in the grid.
   */
  public double _gridRoundUp(Vector grid, double pos) {
    double x = pos - Math.floor(pos);
    int i;
    for (i = 0; i < grid.size() &&
            x >= ((Double) grid.elementAt(i)).doubleValue();
         i++) {
    }
    if (i >= grid.size())
      return pos;
    else
      return Math.floor(pos) + ((Double) grid.elementAt(i)).doubleValue();
  }

  /*
   * Used to find the next value for the axis label.
   * For non-log axes, we just return pos + step.
   * For log axes, we read the appropriate value in the grid Vector,
   * add it to _gridBase and return the sum.  We also take care
   * to reset _gridCurJuke if necessary.
   * Note that for log axes, _gridInit() must be called before
   * calling _gridStep().
   * Based on stepGrid() from xgraph.c by David Harrison.
   */
  public double _gridStep(Vector grid, double pos, double step,
                           boolean logflag) {
    if (logflag) {
      if (++_gridCurJuke >= grid.size()) {
        _gridCurJuke = 0;
        _gridBase += Math.ceil(step);
      }
      if (_gridCurJuke >= grid.size())
        return pos + step;
      return _gridBase +
              ((Double) grid.elementAt(_gridCurJuke)).doubleValue();
    } else {
      return pos + step;
    }
  }

  /*
   * Measure the various fonts.  You only want to call this once.
   */
  public void _measureFonts() {
    // We only measure the fonts once, and we do it from addNotify().
    if (_labelFont == null)
      _labelFont = new Font("Helvetica", Font.PLAIN, 10);
    if (_superscriptFont == null)
      _superscriptFont = new Font("Helvetica", Font.PLAIN, 9);
    if (_titleFont == null)
      _titleFont = new Font("Helvetica", Font.BOLD, 14);

    _labelFontMetrics = getFontMetrics(_labelFont);
    _superscriptFontMetrics = getFontMetrics(_superscriptFont);
    _titleFontMetrics = getFontMetrics(_titleFont);
  }

  /*
   * Return the number of fractional digits required to display the
   * given number.  No number larger than 15 is returned (if
   * more than 15 digits are required, 15 is returned).
   */
  public int _numFracDigits(double num) {
    int numdigits = 0;
    while (numdigits <= 15 && num != Math.floor(num)) {
      num *= 10.0;
      numdigits += 1;
    }
    return numdigits;
  }

  /*
   * Return the number of integer digits required to display the
   * given number.  No number larger than 15 is returned (if
   * more than 15 digits are required, 15 is returned).
   */
  public int _numIntDigits(double num) {
    int numdigits = 0;
    while (numdigits <= 15 && (int) num != 0.0) {
      num /= 10.0;
      numdigits += 1;
    }
    return numdigits;
  }

  /*
   * Parse a string of the form: "word num, word num, word num, ..."
   * where the word must be enclosed in quotes if it contains spaces,
   * and the number is interpreted as a floating point number.  Ignore
   * any incorrectly formatted fields.  I <i>xtick</i> is true, then
   * interpret the parsed string to specify the tick labels on the x axis.
   * Otherwise, do the y axis.
   */
  public void _parsePairs(String line, boolean xtick) {
    // Clear current ticks first.
    if (xtick) {
      _xticks = null;
      _xticklabels = null;
    } else {
      _yticks = null;
      _yticklabels = null;
    }

    int start = 0;
    boolean cont = true;
    while (cont) {
      int comma = line.indexOf(",", start);
      String pair = null;
      if (comma > start) {
        pair = (line.substring(start, comma)).trim();
      } else {
        pair = (line.substring(start)).trim();
        cont = false;
      }
      int close = -1;
      int open = 0;
      if (pair.startsWith("\"")) {
        close = pair.indexOf("\"", 1);
        open = 1;
      } else {
        close = pair.indexOf(" ");
      }
      if (close > 0) {
        String label = pair.substring(open, close);
        String index = (pair.substring(close + 1)).trim();
        try {
          double idx = (Double.valueOf(index)).doubleValue();
          if (xtick)
            addXTick(label, idx);
          else
            addYTick(label, idx);
        } catch (NumberFormatException e) {
          System.err.println("Warning from PlotBox: " +
                             "Unable to parse ticks: " + e.getMessage());
          // ignore if format is bogus.
        }
      }
      start = comma + 1;
      comma = line.indexOf(",", start);
    }
  }

  /*
   * Given a number, round up to the nearest power of ten
   * times 1, 2, or 5.
   *
   * Note: The argument must be strictly positive.
   */
  public double _roundUp(double val) {
    int exponent = (int) Math.floor(Math.log(val) * _LOG10SCALE);
    val *= Math.pow(10, -exponent);
    if (val > 5.0)
      val = 10.0;
    else if (val > 2.0)
      val = 5.0;
    else if (val > 1.0) val = 2.0;
    val *= Math.pow(10, exponent);
    return val;
  }

  /*
   * Internal implementation of setXRange, so that it can be called when
   * autoranging.
   */
  public void _setXRange(double min, double max) {
    // If values are invalid, try for something reasonable.
    if (min > max) {
      min = -1.0;
      max = 1.0;
    } else if (min == max) {
      min -= 1.0;
      max += 1.0;
    }
    // Pad slightly so that we don't plot points on the axes.
    _xMin = min - ((max - min) * _PADDING);
    _xMax = max + ((max - min) * _PADDING);

    // Find the exponent.
    double largest = Math.max(Math.abs(_xMin), Math.abs(_xMax));
    _xExp = (int) Math.floor(Math.log(largest) * _LOG10SCALE);
    // Use the exponent only if it's larger than 1 in magnitude.
    if (_xExp > 1 || _xExp < -1) {
      double xs = 1.0 / Math.pow(10.0, (double) _xExp);
      _xtickMin = _xMin * xs;
      _xtickMax = _xMax * xs;
    } else {
      _xtickMin = _xMin;
      _xtickMax = _xMax;
      _xExp = 0;
    }
  }

  /*
   * Internal implementation of setYRange, so that it can be called when
   * autoranging.
   */
  public void _setYRange(double min, double max) {
    // If values are invalid, try for something reasonable.
    if (min > max) {
      min = -1.0;
      max = 1.0;
    } else if (min == max) {
      min -= 0.1;
      max += 0.1;
    }
    //        if (_yRangeGiven) {
    // The user specified the range, so don't pad.
    //            _yMin = min;
    //            _yMax = max;
    //        } else {
    // Pad slightly so that we don't plot points on the axes.
    _yMin = min - ((max - min) * _PADDING);
    _yMax = max + ((max - min) * _PADDING);
    //        }

    // Find the exponent.
    double largest = Math.max(Math.abs(_yMin), Math.abs(_yMax));
    _yExp = (int) Math.floor(Math.log(largest) * _LOG10SCALE);
    // Use the exponent only if it's larger than 1 in magnitude.
    if (_yExp > 1 || _yExp < -1) {
      double ys = 1.0 / Math.pow(10.0, (double) _yExp);
      _ytickMin = _yMin * ys;
      _ytickMax = _yMax * ys;
    } else {
      _ytickMin = _yMin;
      _ytickMax = _yMax;
      _yExp = 0;
    }
  }

  /*
   *  Zoom in or out based on the box that has been drawn.
   *  The argument gives the lower right corner of the box.
   *  @param x The final x position.
   *  @param y The final y position.
   */
  public synchronized void _zoom(int x, int y) {
    // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
    // it is private!

    // We make this method synchronized so that we can draw the drag
    // box properly.  If this method is not synchronized, then
    // we could end up calling setXORMode, being interrupted
    // and having setPaintMode() called in another method.

    // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
    // not work on mouse drags, thus we have to use this variable
    // to determine whether we are actually zooming. It is used only
    // in _zoomBox, since calling this method is properly masked.
    if (zListener != null) {
      zListener.zoom(x, y);
    }

    _zooming = false;

    Graphics graphics = getGraphics();

    //boolean handled = false;
    if ((_zoomin == true) && (_drawn == true)) {
      if (_zoomxn != -1 || _zoomyn != -1) {
        // erase previous rectangle.
        int minx = Math.min(_zoomx, _zoomxn);
        int maxx = Math.max(_zoomx, _zoomxn);
        int miny = Math.min(_zoomy, _zoomyn);
        int maxy = Math.max(_zoomy, _zoomyn);
        graphics.setXORMode(_boxColor);
        graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
        graphics.setPaintMode();
        // constrain to be in range
        if (y > _lry) y = _lry;
        if (y < _uly) y = _uly;
        if (x > _lrx) x = _lrx;
        if (x < _ulx) x = _ulx;
        // NOTE: ignore if total drag less than 5 pixels.
        if ((Math.abs(_zoomx - x) > 5) && (Math.abs(_zoomy - y) > 5)) {
          double a = _xMin + (_zoomx - _ulx) / _xscale;
          double b = _xMin + (x - _ulx) / _xscale;
          if (a < b)
            setXRange(a, b);
          else
            setXRange(b, a);
          a = _yMax - (_zoomy - _uly) / _yscale;
          b = _yMax - (y - _uly) / _yscale;
          if (a < b)
            setYRange(a, b);
          else
            setYRange(b, a);
        }
        repaint();
        //handled = true;
      }
    } else if ((_zoomout == true) && (_drawn == true)) {
      // Erase previous rectangle.
      graphics.setXORMode(_boxColor);
      int x_diff = Math.abs(_zoomx - _zoomxn);
      int y_diff = Math.abs(_zoomy - _zoomyn);
      graphics.drawRect(_zoomx - 15 - x_diff, _zoomy - 15 - y_diff,
                        30 + x_diff * 2, 30 + y_diff * 2);
      graphics.setPaintMode();

      // Calculate zoom factor.
      double a = (double) (Math.abs(_zoomx - x)) / 30.0;
      double b = (double) (Math.abs(_zoomy - y)) / 30.0;
      double newx1 = _xMax + (_xMax - _xMin) * a;
      double newx2 = _xMin - (_xMax - _xMin) * a;
      // NOTE: To limit zooming out to the fill area, uncomment this...
      // if (newx1 > _xTop) newx1 = _xTop;
      // if (newx2 < _xBottom) newx2 = _xBottom;
      double newy1 = _yMax + (_yMax - _yMin) * b;
      double newy2 = _yMin - (_yMax - _yMin) * b;
      // NOTE: To limit zooming out to the fill area, uncomment this...
      // if (newy1 > _yTop) newy1 = _yTop;
      // if (newy2 < _yBottom) newy2 = _yBottom;
      zoom(newx2, newy2, newx1, newy1);
      //handled = true;
    } else if (_drawn == false) {
      repaint();
      //handled = true;
    }
    _drawn = false;
    _zoomin = _zoomout = false;
    _zoomxn = _zoomyn = _zoomx = _zoomy = -1;
  }

  /*
   *  Draw a box for an interactive zoom box.  The starting point (the
   *  upper left corner of the box) is taken
   *  to be that specified by the startZoom() method.  The argument gives
   *  the lower right corner of the box.  If a previous box
   *  has been drawn, erase it first.
   *  @param x The x position.
   *  @param y The y position.
   */
  public synchronized void _zoomBox(int x, int y) {
    // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
    // it is private!

    // We make this method synchronized so that we can draw the drag
    // box properly.  If this method is not synchronized, then
    // we could end up calling setXORMode, being interrupted
    // and having setPaintMode() called in another method.

    // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
    // not work on mouse drags, thus we have to use this variable
    // to determine whether we are actually zooming.
    if (!_zooming) return;

    Graphics graphics = getGraphics();

    // Bound the rectangle so it doesn't go outside the box.
    if (y > _lry) y = _lry;
    if (y < _uly) y = _uly;
    if (x > _lrx) x = _lrx;
    if (x < _ulx) x = _ulx;
    // erase previous rectangle, if there was one.
    if ((_zoomx != -1 || _zoomy != -1)) {
      // Ability to zoom out added by William Wu.
      // If we are not already zooming, figure out whether we
      // are zooming in or out.
      if (_zoomin == false && _zoomout == false) {
        if (y < _zoomy) {
          _zoomout = true;
          // Draw reference box.
          graphics.setXORMode(_boxColor);
          graphics.drawRect(_zoomx - 15, _zoomy - 15, 30, 30);
        } else if (y > _zoomy) {
          _zoomin = true;
        }
      }

      if (_zoomin == true) {
        // Erase the previous box if necessary.
        if ((_zoomxn != -1 || _zoomyn != -1) && (_drawn == true)) {
          int minx = Math.min(_zoomx, _zoomxn);
          int maxx = Math.max(_zoomx, _zoomxn);
          int miny = Math.min(_zoomy, _zoomyn);
          int maxy = Math.max(_zoomy, _zoomyn);
          graphics.setXORMode(_boxColor);
          graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
        }
        // Draw a new box if necessary.
        if (y > _zoomy) {
          _zoomxn = x;
          _zoomyn = y;
          int minx = Math.min(_zoomx, _zoomxn);
          int maxx = Math.max(_zoomx, _zoomxn);
          int miny = Math.min(_zoomy, _zoomyn);
          int maxy = Math.max(_zoomy, _zoomyn);
          graphics.setXORMode(_boxColor);
          graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
          _drawn = true;
          return;
        } else
          _drawn = false;
      } else if (_zoomout == true) {
        // Erase previous box if necessary.
        if ((_zoomxn != -1 || _zoomyn != -1) && (_drawn == true)) {
          int x_diff = Math.abs(_zoomx - _zoomxn);
          int y_diff = Math.abs(_zoomy - _zoomyn);
          graphics.setXORMode(_boxColor);
          graphics.drawRect(_zoomx - 15 - x_diff, _zoomy - 15 - y_diff,
                            30 + x_diff * 2, 30 + y_diff * 2);
        }
        if (y < _zoomy) {
          _zoomxn = x;
          _zoomyn = y;
          int x_diff = Math.abs(_zoomx - _zoomxn);
          int y_diff = Math.abs(_zoomy - _zoomyn);
          graphics.setXORMode(_boxColor);
          graphics.drawRect(_zoomx - 15 - x_diff, _zoomy - 15 - y_diff,
                            30 + x_diff * 2, 30 + y_diff * 2);
          _drawn = true;
          return;
        } else
          _drawn = false;
      }
    }
    graphics.setPaintMode();
  }

  /*
   *  Set the starting point for an interactive zoom box (the upper left
   *  corner).
   *  @param x The x position.
   *  @param y The y position.
   */
  public synchronized void _zoomStart(int x, int y) {
    // FIXME: This is friendly because Netscape 4.0.3 cannot access it if
    // it is private!

    // constrain to be in range
    if (y > _lry) y = _lry;
    if (y < _uly) y = _uly;
    if (x > _lrx) x = _lrx;
    if (x < _ulx) x = _ulx;
    _zoomx = x;
    _zoomy = y;
    _zooming = true;
  }

  ///////////////////////////////////////////////////////////////////
  ////                         public variables                 ////

  /** @serial Image to draw onto to avoid flashing. */
  public BufferedImage _buffer;

  /** @serial The file to be opened. */
  public String _filespec = null;

  // Call setXORMode with a hardwired color because
  // _background does not work in an application,
  // and _foreground does not work in an applet
  public static final Color _boxColor = Color.darkGray;

  /** @serial The range of the plot as labeled
   * (multiply by 10^exp for actual range.
   */
  public double _ytickMax = 0.0, _ytickMin = 0.0,
  _xtickMax = 0.0 , _xtickMin = 0.0;
  /** @serial The power of ten by which the range numbers should
   *  be multiplied.
   */
  public int _yExp = 0, _xExp = 0;

  /** @serial Scaling used in making tick marks. */
  public double _ytickscale = 0.0, _xtickscale = 0.0;

  /** @serial Font information. */
  public Font _labelFont = null, _superscriptFont = null,
  _titleFont = null;
  /** @serial FontMetric information. */
  public FontMetrics _labelFontMetrics = null,
  _superscriptFontMetrics = null,
  _titleFontMetrics = null;

  // Number format cache used by _formatNum.
  // See the comment in _formatNum for more information.
  // public transient NumberFormat _numberFormat = null;

  // Used for log axes. Index into vector of axis labels.
  public transient int _gridCurJuke = 0;

  // Used for log axes.  Base of the grid.
  public transient double _gridBase = 0.0;

  // An array of strings for reporting errors.
  public transient String _errorMsg[];

  /** @serial The title and label strings. */
  public String _xlabel, _ylabel, _title;

  /** @serial Legend information. */
  public Vector _legendStrings = new Vector(),
  _legendDatasets = new Vector();

  /** @serial If XTicks or YTicks are given/ */
  public Vector _xticks = null, _xticklabels = null,
  _yticks = null, _yticklabels = null;

  // A button for filling the plot
  public transient Button _fillButton = null;

  // Variables keeping track of the interactive zoom box.
  // Initialize to impossible values.
  public transient int _zoomx = -1;
  public transient int _zoomy = -1;
  public transient int _zoomxn = -1;
  public transient int _zoomyn = -1;

  // Control whether we are zooming in or out.
  public transient boolean _zoomin = false;
  public transient boolean _zoomout = false;
  public transient boolean _drawn = false;
  public transient boolean _zooming = false;

  // NOTE: It is unfortunate to have to include the DTD here, but there
  // seems to be no other way to ensure that the generated data exactly
  // matches the DTD.
  public static final String _DTD =
          "<!-- PlotML DTD, created by Edward A. Lee, eal@eecs.berkeley.edu. -->"
          + "<!ELEMENT plot (barGraph | bin | dataset | default | noColor | \n"
          + "	noGrid | title | wrap | xLabel | xLog | xRange | xTicks | yLabel | \n"
          + " yLog | yRange | yTicks)*>\n"
          + "  <!ELEMENT barGraph EMPTY>\n"
          + "    <!ATTLIST barGraph width CDATA #IMPLIED>\n"
          + "    <!ATTLIST barGraph offset CDATA #IMPLIED>\n"
          + "  <!ELEMENT bin EMPTY>\n"
          + "    <!ATTLIST bin width CDATA #IMPLIED>\n"
          + "    <!ATTLIST bin offset CDATA #IMPLIED>\n"
          + "  <!ELEMENT dataset (m | move | p | point)*>\n"
          + "    <!ATTLIST dataset connected (yes | no) #IMPLIED>\n"
          + "    <!ATTLIST dataset marks (none | dots | points | various) #IMPLIED>\n"
          + "    <!ATTLIST dataset name CDATA #IMPLIED>\n"
          + "    <!ATTLIST dataset stems (yes | no) #IMPLIED>\n"
          + "  <!ELEMENT default EMPTY>\n"
          + "    <!ATTLIST default connected (yes | no) \"yes\">\n"
          + "    <!ATTLIST default marks (none | dots | points | various) \"none\">\n"
          + "    <!ATTLIST default stems (yes | no) \"no\">\n"
          + "  <!ELEMENT noColor EMPTY>\n"
          + "  <!ELEMENT noGrid EMPTY>\n"
          + "  <!ELEMENT title (#PCDATA)>\n"
          + "  <!ELEMENT wrap EMPTY>\n"
          + "  <!ELEMENT xLabel (#PCDATA)>\n"
          + "  <!ELEMENT xLog EMPTY>\n"
          + "  <!ELEMENT xRange EMPTY>\n"
          + "    <!ATTLIST xRange min CDATA #REQUIRED>\n"
          + "    <!ATTLIST xRange max CDATA #REQUIRED>\n"
          + "  <!ELEMENT xTicks (tick)+>\n"
          + "  <!ELEMENT yLabel (#PCDATA)>\n"
          + "  <!ELEMENT yLog EMPTY>\n"
          + "  <!ELEMENT yRange EMPTY>\n"
          + "    <!ATTLIST yRange min CDATA #REQUIRED>\n"
          + "    <!ATTLIST yRange max CDATA #REQUIRED>\n"
          + "  <!ELEMENT yTicks (tick)+>\n"
          + "    <!ELEMENT tick EMPTY>\n"
          + "      <!ATTLIST tick label CDATA #REQUIRED>\n"
          + "      <!ATTLIST tick position CDATA #REQUIRED>\n"
          + "    <!ELEMENT m EMPTY>\n"
          + "      <!ATTLIST m x CDATA #IMPLIED>\n"
          + "      <!ATTLIST m x CDATA #REQUIRED>\n"
          + "      <!ATTLIST m lowErrorBar CDATA #IMPLIED>\n"
          + "      <!ATTLIST m highErrorBar CDATA #IMPLIED>\n"
          + "    <!ELEMENT move EMPTY>\n"
          + "      <!ATTLIST move x CDATA #IMPLIED>\n"
          + "      <!ATTLIST move x CDATA #REQUIRED>\n"
          + "      <!ATTLIST move lowErrorBar CDATA #IMPLIED>\n"
          + "      <!ATTLIST move highErrorBar CDATA #IMPLIED>\n"
          + "    <!ELEMENT p EMPTY>\n"
          + "      <!ATTLIST p x CDATA #IMPLIED>\n"
          + "      <!ATTLIST p x CDATA #REQUIRED>\n"
          + "      <!ATTLIST p lowErrorBar CDATA #IMPLIED>\n"
          + "      <!ATTLIST p highErrorBar CDATA #IMPLIED>\n"
          + "    <!ELEMENT point EMPTY>\n"
          + "      <!ATTLIST point x CDATA #IMPLIED>\n"
          + "      <!ATTLIST point x CDATA #REQUIRED>\n"
          + "      <!ATTLIST point lowErrorBar CDATA #IMPLIED>\n"
          + "      <!ATTLIST point highErrorBar CDATA #IMPLIED>";


  ///////////////////////////////////////////////////////////////////
  ////                         inner classes                     ////

  class FillButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      fillPlot();
    }
  }

  public class PtZoomListener implements MouseListener {
    public void mouseClicked(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    public void mousePressed(MouseEvent event) {
      // http://developer.java.sun.com/developer/bugParade/bugs/4072703.html
      // BUTTON1_MASK still not set for MOUSE_PRESSED events
      // suggests:
      // Workaround
      //   Assume that a press event with no modifiers must be button 1.
      //   This has the serious drawback that it is impossible to be sure
      //   that button 1 hasn't been pressed along with one of the other
      //   buttons.
      // This problem affects Netscape 4.61 under Digital Unix and
      // 4.51 under Solaris
      if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ||
              event.getModifiers() == 0) {
        RepastPlotBox.this._zoomStart(event.getX(), event.getY());
      }
    }

    public void mouseReleased(MouseEvent event) {
      if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ||
              event.getModifiers() == 0) {
        RepastPlotBox.this._zoom(event.getX(), event.getY());
      }
    }
  }

  public class DragListener implements MouseMotionListener {
    public void mouseDragged(MouseEvent event) {
      // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON1_MASK does
      // not work on mouse drags.  It does work on MouseListener
      // methods, so those methods set a variable _zooming that
      // is used by _zoomBox to determine whether to draw a box.
      // if ((event.getModifiers() & event.BUTTON1_MASK)!= 0) {
      RepastPlotBox.this._zoomBox(event.getX(), event.getY());
      // }
    }

    public void mouseMoved(MouseEvent event) {
    }
  }

  class CommandListener implements KeyListener {
    public void keyPressed(KeyEvent e) {
      int keycode = e.getKeyCode();
      switch (keycode) {
        case KeyEvent.VK_CONTROL:
          _control = true;
          break;
        case KeyEvent.VK_SHIFT:
          _shift = true;
          break;
        case KeyEvent.VK_C:
          if (_control) {
            // The "null" sends the output to the clipboard.
            export(null);
            Message message = new Message(
                    "Encapsulated PostScript (EPS) " +
                    "exported to clipboard.",
                    null, null, 1, 60, TextArea.SCROLLBARS_NONE);
            message.setTitle("Ptolemy Plot Message");
          }
          break;
        case KeyEvent.VK_D:
          if (!_control && _shift) {
            write(System.out);
            Message message = new Message(
                    "Plot data sent to standard out.",
                    null, null, 1, 40, TextArea.SCROLLBARS_NONE);
            message.setTitle("Ptolemy Plot Message");
          }
          if (_control) {
            // xgraph and many other Unix apps use Control-D to exit
            System.exit(1);
          }
          break;
        case KeyEvent.VK_E:
          if (!_control && _shift) {
            export(System.out);
            Message message = new Message(
                    "Encapsulated PostScript (EPS) " +
                    "exported to standard out.",
                    null, null, 1, 60, TextArea.SCROLLBARS_NONE);
            message.setTitle("Ptolemy Plot Message");
          }
          break;
        case KeyEvent.VK_F:
          if (!_control && _shift) {
            fillPlot();
          }
          break;
        case KeyEvent.VK_H:
          if (!_control && _shift) {
            _help();
          }
          break;

        case KeyEvent.VK_SLASH:
          if (_shift) {
            // Question mark is SHIFT-SLASH
            _help();
          }
          break;
        default:
          // None
      }
    }

    public void keyReleased(KeyEvent e) {
      int keycode = e.getKeyCode();
      switch (keycode) {
        case KeyEvent.VK_CONTROL:
          _control = false;
          break;
        case KeyEvent.VK_SHIFT:
          _shift = false;
          break;
        default:
          // None
      }
    }

    // The keyTyped method is broken in jdk 1.1.4.
    // It always gets "unknown key code".
    public void keyTyped(KeyEvent e) {
    }

    public boolean _control = false;
    public boolean _shift = false;
  }

  public BufferedImage getImage() {
    Dimension d = this.getSize();
    BufferedImage bi = (BufferedImage) this.createImage(d.width, d.height);
    Graphics g = bi.getGraphics();
    this.paint(g);
    g.dispose();

    return bi;
  }
}
