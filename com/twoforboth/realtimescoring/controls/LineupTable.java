package com.twoforboth.realtimescoring.controls;

import com.twoforboth.controls.Swing.StandardTable;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;


import java.lang.Thread;
import java.util.ArrayList;
import java.util.Vector;
import java.util.EventObject;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Table representing a current week's lineup.  Thus table<BR>
 * supports real-time scoring updates that trigger an individual cell to blink<BR>
 * a particular color based on the score either increasing or decreasing.<BR>
 * Other cells will be updated based on the status of a game (started, ended,<BR>
 * etc.)
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class LineupTable extends StandardTable implements TableListener //implements TableModelListener
{
  private ArrayList blinkThreads_ = new ArrayList();         //Array of BlinkThread objects
  private ArrayList threads_ = new ArrayList();              //Corresponding array of thread objects
  private int blinkColumnIndex_ = -1;                        //Column to blink
  private int blinks_ = -1;                                  //Number of times to blink the cell
  private int blinkDuration_ = -1;                           //Number of seconds between blinks
  private Color negativeBlinkColor_ = this.getBackground();  //Color to blink when value of cell decreases
  private Color positiveBlinkColor_ = this.getBackground();  //Color to blink when value of cell increases

  /**
   * Constructor
   * @param name Table name for identification
   * @param d Dimension for table
   * @param columnHeaders Array of strings representing the column titles
   * @param columnWidths Array of integers for column widths
   * @param blinkColumnName Name of the column that will blink
   * @param blinks Number of times to blink the cell
   * @param blinkDuration Number of seconds between blinks
   * @param negativeBlinkColor Color to blink when new value is less than old value
   * @param positiveBlinkColor Color to blink when new value is greater than old value
   * @param colorColumnNames Array of cells that will have their background colors affected (non blinking)
   */

  public LineupTable(String name,
		     Dimension d,
		     String[] columnHeaders,
		     int[] columnWidths,
		     String blinkColumnName,
		     int blinks,
		     int blinkDuration,
		     Color negativeBlinkColor,
		     Color positiveBlinkColor,
		     String[] colorColumnNames)
  {
    //Initialize the base object
    super(name, d, columnHeaders, columnWidths);
    //This table does not allow any type of selection
    this.setCellSelectionEnabled(false);
    //Create a new listener object for table events
    LineupTableListener ltl = new LineupTableListener();
    //Have the model listen for certain events
    this.getModel().addTableModelListener(ltl);
    //Make this table listen for it's own events generated by the model
    ltl.addListener(this);

    if (blinkColumnName.length() > 0)
    {
      try
      {
	//Get the column to blink by name
	TableColumn blinkColumn = this.getColumn(blinkColumnName);
	//Assign the renderer
	blinkColumn.setCellRenderer(new BlinkingRenderer(blinkThreads_));
	//Get the index and store it
	blinkColumnIndex_ = blinkColumn.getModelIndex();
	blinks_ = blinks;
	blinkDuration_ = blinkDuration;
	negativeBlinkColor_ = negativeBlinkColor;
	positiveBlinkColor_ = positiveBlinkColor;
      }
      catch (java.lang.IllegalArgumentException iae)
      {
	blinkColumnIndex_ = -1;
      }
    }
    //Set up the colummns to have their background colors affected by game state
    //New 10/12/02
    for (int i = 0; i < colorColumnNames.length; i++)
    {
      try
      {
         TableColumn colorColumn = this.getColumn(colorColumnNames[i]);
         //Assign the renderer
         colorColumn.setCellRenderer(new BackgroundRenderer());
      }
      catch (java.lang.IllegalArgumentException e)
      {
	System.out.println("LineupTable constructor attempted to assign column with name " + colorColumnNames[i] + " failed");
      }
    }
  }

  /**
   * Listener event for one or more rows being inserted in the table.  When<BR>
   * this event occurs, a new thread is created for that row's blinking cell.
   * @param RowInsertedEvent Event object that will contain the number of rows
   * inserted
   */
  public void rowInserted(RowInsertedEvent evt)
  {
    if (blinkColumnIndex_ != -1)
    {
      int firstRowInserted = evt.getFirstRow();
      int lastRowInserted = evt.getLastRow();
      for (int i = firstRowInserted; i < (lastRowInserted + 1); i++)
      {
	//Create the new Runnable object controling the blinking
	BlinkThread bt = new BlinkThread(this,
	                                 i,
					 blinkColumnIndex_,
					 blinks_,
					 blinkDuration_,
					 positiveBlinkColor_);
	//Add the object to the array
	blinkThreads_.add(bt);
	//Create a new thread object containing the runnable object
	Thread t = new Thread(bt);
	//Make the applet own the thread for clean shutdown
	t.setDaemon(true);
	//Add the thread to the array
	threads_.add(t);
      }
    }
  }

  /**
   * Method to initiate the updating and blinking of the blink cell (technically)
   * this method should determine if the column in question is the blinking column
   * and either call setValueAt directly or a private method that does what the
   * rest of this function does
   * @param value Integer new value
   * @param row table row
   * @param col table column
   */

  public void updateScoreValue(int value, int row, int col)
  {
    //Only check for blinking if the blink column has been defined
    if (blinkColumnIndex_ != -1)
    {
      //Build the id for the thread to search for in the form rRowcCol
      String searchID; //"r" + row + "c" + col;
      StringBuffer sb = new StringBuffer();
      sb.append('r');
      sb.append(row);
      sb.append('c');
      sb.append(col);

      searchID = sb.toString();
      int foundIndex = -1;
      BlinkThread blinkThread = null;
      //Search for the proper thread based on id
      int max = blinkThreads_.size();
      for (int i = 0; i < max; i++)
      {
	blinkThread = (BlinkThread)blinkThreads_.get(i);
	if (blinkThread.getID().equals(searchID))
	{
	  foundIndex = i;
	  break;
	}
      }

      if (foundIndex != -1)
      {
	//Don't start blinking if the cell was already blinking
	if (blinkThread.isRunning() == false)
	{
	  int oldValue = 0;
	  //Get the original value
	  try
	  {
	    oldValue = ((Integer)this.getValueAt(row, col)).intValue();
	  }
	  catch (java.lang.NumberFormatException nfe1)
	  {
	    oldValue = -1;
	  }
	  //If the value changed
	  if (oldValue != value)
	  {
	    //Update the cell
	    this.setValueAt(new Integer(value), row, col);

	    //If the original thread was already dead (i.e. run and stopped)
	    //Create a new thread
	    if (blinkThread.getKilled() == true)
	    {
	      threads_.set(foundIndex, new Thread(blinkThread));
	    }
	    //Determine the color to blink based on original and new values
	    if (oldValue > value)
	    {
	      blinkThread.setBlinkColor(negativeBlinkColor_);
	    }
	    else
	    {
	      blinkThread.setBlinkColor(positiveBlinkColor_);
	    }
	    //Get the associated Thread object from the runnable array
	    Thread t = (Thread)threads_.get(foundIndex);
	    //Begin blinking
	    t.start();
	  }
	}
	else
	{
	  this.setValueAt(new Integer(value), row, col);
	}
      }
      else
      {
	this.setValueAt(new Integer(value), row, col);
      }
    }
    else
    {
      this.setValueAt(new Integer(value), row, col);
    }
  }

  /**
   * Default table updating -- no blinking
   * @param value New value to update
   * @param row Table row
   * @param col Table column
   */
  public void setValueAt(Object value, int row, int col)
  {
     super.setValueAt(value, row, col);
  }
}

/**
 * LineupTableListener is an inner class for the LineupTable.  This class allows
 * the table to listen for it's own events such as row insertion.
 */

class LineupTableListener implements TableModelListener
{
  //Listeners
  private Vector tableListeners_ = new Vector();

  public synchronized void addListener(TableListener l)
  {
    //add a listener if it is not already registered
    if (!tableListeners_.contains(l))
    {
      tableListeners_.add(l);
    }
  }

  public synchronized void removeListener(TableListener l)
  {
    //remove listener if registered
    if (tableListeners_.contains(l))
    {
      tableListeners_.remove(l);
    }
  }

  /**
  * Method called when cell values change, table stucture changes,
  * row are inserted / deleted / updated or if an individual cell
  * is updated
  * @param e TableModelEvent contains the event information
  */
  public void tableChanged(TableModelEvent e)
  {
    if (e.getType() == TableModelEvent.INSERT)
    {
      //System.out.println("tableChanged Row inserted first = " + e.getFirstRow() + " last = " + e.getLastRow());
      RowInsertedEvent evt = new RowInsertedEvent(this, e.getFirstRow(), e.getLastRow());
      Vector v;
      synchronized(this)
      {
	v = (Vector)tableListeners_.clone();
      }
      //Fire the event to all listeners
      int cnt = v.size();
      for (int i = 0; i < cnt; i++)
      {
	TableListener client = (TableListener)v.elementAt(i);
	client.rowInserted(evt);
      }
    }
  }
}

/**
 * Allows for objects to listen for this table's events.  This is an inner
 * interface because only the table is interested in listening to it's own
 * events.
 */

interface TableListener extends java.util.EventListener
{
  void rowInserted(RowInsertedEvent evt);
}

/**
 * RowInsertedEvent is the event that takes place when a row has been inserted
 * into the table.  It contains the first and last row indices that have been
 * inserted.
 */

class RowInsertedEvent extends EventObject
{
  protected int firstRow_ = 0;
  protected int lastRow_ = 0;

  public RowInsertedEvent(Object source, int firstRow, int lastRow)
  {
    super(source);
    firstRow_ = firstRow;
    lastRow_ = lastRow;
  }

  public int getFirstRow()
  {
    return firstRow_;
  }

  public int getLastRow()
  {
    return lastRow_;
  }
}

/**
 * Idea -- have value sent to renderer be value,color
 */

class BackgroundRenderer extends DefaultTableCellRenderer
{
  private StringTokenizer st_ = null;

  /**
   * Overridden method that allows for the updating of the renderer directly
   * @param table Table being modified
   * @param value New value to update the cell with
   * @param isSelected Boolean true if cell is currently selected
   * @param hasFocus Boolean true if cell currently has the focus
   * @param row Cell row
   * @param col Cell Column
   * @returns Component (cell)
   */

  public Component getTableCellRendererComponent(JTable table,
						 Object value,
						 boolean isSelected,
						 boolean hasFocus,
						 int row,
						 int col)
  {
    String fullString = value.toString();
    String display = fullString;
    String colorString = "";

//    System.out.println("Cell Renderer fullString = " + fullString);

    if (fullString.indexOf("~") != -1)
    {
      st_ = new StringTokenizer(fullString, "~", false);
      display = st_.nextToken();
      colorString = st_.nextToken();
      Color c = new Color(new Integer(colorString).intValue());
      //System.out.println("Cell renderer display = " + display + " color = " + c.toString());
      this.setBackground(c);
    }
    //Update the renderer
    this.setValue(display);

    return this;
  }
}

/**
 * BlinkingRenderer is an inner class specific to tables that will have one or
 * more cells that can blink.  The renderer is passed a copy of the array
 * containing the threads that control the blinking.  Each of these objects
 * contains an id containing the row and column information for each cell.
 */

class BlinkingRenderer extends DefaultTableCellRenderer
{
  private ArrayList blinkThreads_ = null;
  private int blinkColumn_ = -1;
  private Color originalColor_ = this.getBackground();

  /**
   * Constructor
   * @param blinkThreads Array of threads each controling the blinking of a cell
   */

  public BlinkingRenderer(ArrayList blinkThreads)
  {
    blinkThreads_ = blinkThreads;
  }

  /**
   * Overridden method that allows for the updating of the renderer directly
   * @param table Table being modified
   * @param value New value to update the cell with
   * @param isSelected Boolean true if cell is currently selected
   * @param hasFocus Boolean true if cell currently has the focus
   * @param row Cell row
   * @param col Cell Column
   * @returns Component (cell)
   */

  public Component getTableCellRendererComponent(JTable table,
						 Object value,
						 boolean isSelected,
						 boolean hasFocus,
						 int row,
						 int col)
  {
    BlinkThread blinkThread = null;

    //Find the associated blinkThread for row and col
    String searchID; //"r" + row + "c" + col;
    StringBuffer sb = new StringBuffer();
    sb.append('r');
    sb.append(row);
    sb.append('c');
    sb.append(col);
    searchID = sb.toString();
    int max = blinkThreads_.size();
    for (int i = 0; i < max; i++)
    {
      blinkThread = (BlinkThread)blinkThreads_.get(i);
      if (blinkThread.getID().equals(searchID))
      {
	break;
      }
    }

    if (blinkThread != null)
    {
      //Determine which phase of blinking is currently occuring
      if ((blinkThread.getCount() % 2) == 0)
      {
	this.setBackground(originalColor_);
      }
      else
      {
	this.setBackground(blinkThread.getBlinkColor());
      }
    }
    //Set the alignment for this field (should be a parameter)
    this.setHorizontalAlignment(SwingConstants.RIGHT);
    //Update the renderer
    this.setValue(value);

    return this;
  }
}

/**
 * Inner class representing the thread that blinks the cell.  Each thread
 * contains the id of the cell in the form rRowcCol and number of times it
 * has blinked.
 */

class BlinkThread implements Runnable
{
  private int row_ = 0;                        //Row of cell to blink
  private int col_ = 0;                        //Column of cell to blink
  private int count_ = 0;                      //Number of times the cell has blinked
  private String id_ = "";                     //Id of cell in the form rRowcCol
  private boolean running_ = false;            //Flag to keep the cell running
  private long sleepTime_ = 0L;                //Number of milliseconds between running
  private JTable table_ = null;                //Table that the cell belongs to
  private Object value_ = null;                //Value of cell object
  private boolean killed_ = false;             //Flag indicating when the cell has stopped running
  private Color blinkColor_ = Color.white;     //Blink color (set in the constructor)
  private int blinks_ = 0;                     //Number of times cell should blink before thread is killed

  private Runnable runnable;

  /**
   * Constructor
   * @table Table that the cell belongs to
   * @row Row of cell to blink
   * @col Column of cell to blink
   * @blinks Number of times cell should blink before thread is killed
   * @seconds Number of seconds between blinks
   * @blinkColor Blink color
   */
  public BlinkThread(JTable table,
		     int row,
		     int col,
		     int blinks,
		     int seconds,
		     Color blinkColor)
  {
    table_ = table;
    row_ = row;
    col_ = col;

    StringBuffer sb = new StringBuffer();
    sb.append('r');
    sb.append(row);
    sb.append('c');
    sb.append(col);
    id_ = sb.toString();
    blinks_ = blinks * 2;
    sleepTime_ = seconds * 1000;
    blinkColor_ = blinkColor;

    //10/13/02 Makes the updating of the cell thread safe because this code
    //is executed in the event dispatch thread
    runnable = new Runnable()
    {
      public void run()
      {
	value_ = table_.getValueAt(row_, col_);
	table_.setValueAt(value_, row_, col_);
      }
    };
  }

  //Getters
  public int getCount() { return count_; }
  public String getID() { return id_; }
  public boolean isRunning() { return running_; }
  public boolean getKilled() { return killed_; }
  public Color getBlinkColor() { return blinkColor_; }
  //Setters
  public void setBlinkColor(Color blinkColor) { blinkColor_ = blinkColor; }
  //Counter methods
  public void incrementCount() { count_++; }
  public void resetCount() { count_ = 0; }

  /**
   * Main thread method
   */

  public void run()
  {
    //Indicate thread is running
    running_ = true;
    //Reset the counter for number of blinks
    resetCount();

    try
    {
      while(running_)
      {
	Thread.sleep(sleepTime_);
	//Bump the counter
	incrementCount();

//Moved to runable 10/13/02 -- This makes the updating of the cell thread safe
	//Make sure the table is updated
//	value_ = table_.getValueAt(row_, col_);
//	table_.setValueAt(value_, row_, col_);
	SwingUtilities.invokeLater(runnable);
//

	//If the number of blinks has been reached, kill this thread
	if (getCount() == blinks_)
	{
	  //Mark it as killed
	  killed_ = true;
	  running_ = false;
	}
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}