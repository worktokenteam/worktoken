package com.worktoken.model;

import com.worktoken.engine.TimeExpression;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Entity
@NamedQueries(
    @NamedQuery(name = "TimerTrigger.findAlerts", query = "SELECT t FROM TimerTrigger t WHERE t.armed = TRUE AND t.nextAlarm <= :date ")
)
public class TimerTrigger extends EventTrigger {
    String expression;
    @Embedded
    TimeExpression timeExpression;
    @Enumerated(EnumType.STRING)
    TimerTriggerType triggerType;
    Date nextAlarm;
    private boolean armed;

    public boolean isArmed() {
        return armed;
    }

    public TimerTrigger() {
        timeExpression = new TimeExpression();
    }

    public TimeExpression getTimeExpression() {
        return timeExpression;
    }

    public void setTimeExpression(TimeExpression timeExpression) {
        this.timeExpression = timeExpression;
    }

    public TimerTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TimerTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        timeExpression.parse(expression);
    }

    public Date getNextAlarm() {
        return nextAlarm;
    }

    public void setNextAlarm(Date nextAlarm) {
        this.nextAlarm = nextAlarm;
    }

    /**
     * Activates new trigger
     *
     * Do not use it to reactivate a trigger!
     */
    public void arm() {
        /*
        if already armed, throw exception
         */
        if (nextAlarm != null) {
            throw new IllegalStateException("Trying to arm already armed time trigger, " + getDefinitionId());
        }
        DateTime dt;
        switch (triggerType) {
            case Duration:
                nextAlarm = timeExpression.calculateDurationAlarmDate();
                if (nextAlarm == null) {
                    throw new IllegalStateException("Trying to setup expired duration timer");
                }
                armed = true;
            // TODO: cycle and date
        }
    }

    public void disarm() {
        armed = false;
    }
}
