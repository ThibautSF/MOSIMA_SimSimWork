package uchicago.src.sim.engine;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.util.Random;

/**
 * Tests randomization of list in generated basic actions calling super
 * class method of list object.
 */
public class ScheduleListTestSuper extends TestCase {
  
  public ArrayList list = new ArrayList();
  public Schedule schedule;
  public String order;
  public String newOrder;

  public ScheduleListTestSuper(String name) {
    super(name);
  }

  public void setUp() {
    Random.createUniform();
    schedule = new Schedule();
    
    list = new ArrayList();
    
    for (int i = 0; i < 10; i++) {
      list.add(new ScheduleTestAgentOne(i, schedule));
    }

    for (int i = 10; i < 20; i++) {
       list.add(new ScheduleTestAgentTwo(i, schedule));
    }

    order = new String();
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      order += a.getId() + " ";
    }

    //System.out.println("order: " + order);
    
  }

  public String getTicks() {
    return ((ScheduleTestAgent)list.get(0)).getTicks();
  }

   public void orderComp() {
    orderComp(10);
  }

  public void orderComp(int runTime) {
    runSchedule(runTime);
    newOrder = "";
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      newOrder += a.getId() + " ";
    }
  }

  public void testAtRnd() {
    schedule.scheduleActionAtRnd(3, list, ScheduleTestAgent.class, "printId");
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 ".equals(getTicks()));
  }

  public void testAtRndLast() {
    schedule.scheduleActionAtRnd(4, list, ScheduleTestAgent.class, "printId", Schedule.LAST);
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("4.0 ".equals(getTicks()));
  }

  public void testIntervalRnd() {
    schedule.scheduleActionAtIntervalRnd(3, list, ScheduleTestAgent.class, "printId");
    orderComp(3);
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 6.0 9.0 ".equals(getTicks()));
  }

  public void testIntervalLastRnd() {
    schedule.scheduleActionAtIntervalRnd(3, list, ScheduleTestAgent.class, "printId", Schedule.LAST);
    orderComp(3);
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 6.0 9.0 ".equals(getTicks()));
  }

   public void testBeginningRnd() {
    schedule.scheduleActionBeginningRnd(1, list, ScheduleTestAgent.class, "printId");
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 ".equals(getTicks()));
  }
    

  public void testOrder() {
    String newOrder = "";
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      newOrder += a.getId() + " ";
    }
    assertTrue("unshuffled != unshuffled", order.equals(newOrder));
  }

  public void runSchedule(int runTime) {
    //System.out.print("new order: ");
    for (int i = 0; i < runTime; i++) {
      schedule.execute();
    }
  }

  public void runSchedule() {
    runSchedule(10);
  }

  public static junit.framework.Test suite() {
    return new TestSuite(uchicago.src.sim.engine.ScheduleListTestSuper.class);
  }
}
 
