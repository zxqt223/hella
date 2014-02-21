package com.zhangyue.hella.engine.core.job.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @Descriptions The class JobEventStorehouse.java's implementation：Job事件仓库
 *
 * @author scott 
 * @date 2013-8-19 下午2:08:35
 * @version 1.0
 */
public class JobEventStorehouse {
  private static Logger log = Logger.getLogger(JobEventStorehouse.class);
  private static int DEFAULTSIZE = 3;

  private int base = 0;
  private int top = 0;
  private Event[] products = new Event[10];

  public JobEventStorehouse() {
    products = new Event[JobEventStorehouse.DEFAULTSIZE];
  }

  public JobEventStorehouse(int resourceSize) {
    super();
    products = new Event[resourceSize > 0 ? resourceSize : JobEventStorehouse.DEFAULTSIZE];
  }

  public synchronized void push(Event product) {
    log.info("JobEvent push Event :" + product.toString());
    while (top == products.length) {
      try {
        log.info("JobEvent Storehous is full，wait for consumed ...");
        wait();
      } catch (InterruptedException e) {
        log.error("stop push product because other reasons，Exception：" + e.getMessage());
      }
    }
    products[top] = product;
    top++;

    notify();
  }

  public synchronized Event pop() {
    log.info("JobEvent pop Event ");
    Event pro = null;
    while (top == base) {
      try {
        log.info("JobEvent Storehous is empty，wait for produce ...");
        wait();
      } catch (InterruptedException e) {
        log.error("stop push product because other reasons,Exception：" + e.getMessage());

      }
    }
    top--;
    pro = products[top];
    products[top] = null;
    notify();

    return pro;
  }

  public synchronized String[] popAll() {
    log.info("JobEvent Storehouse begin emptying...");
    if (products.length == 0) {
      return null;
    }
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < products.length; i++) {
      if(null==products[i]||null==products[i].getName()){
        continue;
      }
      list.add(products[i].getName());
      products[i] = null;
    }
    top =0;
    return list.toArray(new String[list.size()]);
  }
}
