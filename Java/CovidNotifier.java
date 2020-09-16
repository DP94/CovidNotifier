import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class CovidNotifier {

    public static void main(String[] args) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(CovidNotifierJob.class).withIdentity("Covid Notifier", "group1").build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("Covid trigger", "group1").withSchedule(CronScheduleBuilder.cronSchedule("0 30 16 * * ?")).build();
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.scheduleJob(job, trigger);
        scheduler.start();
    }
}