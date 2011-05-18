package com.worktoken.engine;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import javax.persistence.Embeddable;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Alex Pavlov (alex@rushproject.com)
 */
@Embeddable
public class TimeExpression {

    private Date startDate;
    private Date endDate;
    private String duration;
    private Integer repeatCount;

    private static Pattern durationPattern;
    private static Pattern repeatPattern;


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void parse(final String expression) {
        String[] parts = expression.split("/");
        int index = 0;

        if (parts[0] != null) {
            if (isRepeat(parts[0])) {
                repeatCount = Integer.parseInt(parts[0].substring(1));
                index = 1;
            }
        }
        if (index < parts.length) {
            /*
           Next is either start date or duration
            */
            if (isDuration(parts[index])) {
                duration = parts[index];
                ++index;
                if (index < parts.length) {
                    /*
                   next is end date
                    */
                    endDate = parseDateTime(parts[index]);
                    return;
                }
            } else {
                startDate = parseDateTime(parts[index]);
                ++index;
                if (index < parts.length) {
                    /*
                   next is either duration or end date
                    */
                    if (isDuration(parts[index])) {
                        duration = parts[index];
                        ++index;
                    } else {
                        endDate = parseDateTime(parts[index]);
                        return;
                    }
                    if (index < parts.length) {
                        /*
                       next is end date
                        */
                        endDate = parseDateTime(parts[index]);
                        return;
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid time expression \"" + expression + "\"");
        }
    }

    private Date parseDateTime(String dts) {
        DateTime dt = new DateTime(dts);
        return dt.toDate();
    }

    private static boolean isRepeat(String part) {
        return getRepeatPattern().matcher(part).find();
    }

    public static Pattern getDurationPattern() {
        if (durationPattern == null) {
            durationPattern = Pattern.compile("^p", Pattern.CASE_INSENSITIVE);
        }
        return durationPattern;
    }

    public static Pattern getRepeatPattern() {
        if (repeatPattern == null) {
            repeatPattern = Pattern.compile("^r\\d+$", Pattern.CASE_INSENSITIVE);
        }
        return repeatPattern;
    }

    private static boolean isDuration(String expression) {
        return getDurationPattern().matcher(expression).find();
    }

    /**
     * Calculates alarm date for time duration elements
     *
     * @return Alarm date or null, if alarm date is in the past
     */
    public Date calculateDurationAlarmDate() {
        DateTime now = new DateTime();
        if (endDate != null) {
            return endDate;
        }
        Date start;
        if (startDate != null) {
            start = startDate;
        } else if (duration != null) {
            start = now.toDate();
        } else {
            throw new IllegalStateException("Invalid duration definition");
        }
        Period period = new Period(duration);
        Duration dur = period.toStandardDuration();
        DateTime alarm;
        if (dur.getStandardSeconds() > 24*60*60) {
            alarm = new DateTime(start).plus(period);
        } else {
            alarm = new DateTime(start).plus(dur);
        }
        if (alarm.isBefore(now)) {
            return null;
        }
        return alarm.toDate();
    }
}
