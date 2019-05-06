package org.saber.saberschtaskwanip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@SpringBootApplication
@EnableScheduling
public class SaberSchtaskWanipApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SaberSchtaskWanipApplication.class, args);
    }


//    public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
//            implements ApplicationContextAware {
//        private transient AutowireCapableBeanFactory beanFactory;
//
//        @Override
//        public void setApplicationContext(final ApplicationContext context) {
//            beanFactory = context.getAutowireCapableBeanFactory();
//        }
//    }
//    @Bean
//    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext context) {
//        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
//        jobFactory.setApplicationContext(context);
//        return jobFactory;
//    }

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 当前设置每10分钟执行一次
     * cron = "* * * * * *"
     *
     * {秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
     *
     * {秒数}{分钟} ==> 允许值范围: 0~59 ,不允许为空值，若值不合法，调度器将抛出SchedulerException异常
     * “*” 代表每隔1秒钟触发；
     * “,” 代表在指定的秒数触发，比如”0,15,45”代表0秒、15秒和45秒时触发任务
     * “-“代表在指定的范围内触发，比如”25-45”代表从25秒开始触发到45秒结束触发，每隔1秒触发1次
     * “/”代表触发步进(step)，”/”前面的值代表初始值(““等同”0”)，后面的值代表偏移量，比如”0/20”或者”/20”代表从0秒钟开始，每隔20秒钟触发1次，即0秒触发1次，20秒触发1次，40秒触发1次；”5/20”代表5秒触发1次，25秒触发1次，45秒触发1次；”10-45/20”代表在[10,45]内步进20秒命中的时间点触发，即10秒触发1次，30秒触发1次
     * {小时} ==> 允许值范围: 0~23 ,不允许为空值，若值不合法，调度器将抛出SchedulerException异常,占位符和秒数一样
     * {日期} ==> 允许值范围: 1~31 ,不允许为空值，若值不合法，调度器将抛出SchedulerException异常
     * {星期} ==> 允许值范围: 1~7 (SUN-SAT),1代表星期天(一星期的第一天)，以此类推，7代表星期六(一星期的最后一天)，不允许为空值，若值不合法，调度器将抛出SchedulerException异常
     * {年份} ==> 允许值范围: 1970~2099 ,允许为空，若值不合法，调度器将抛出SchedulerException异常
     *
     * 注意：除了{日期}和{星期}可以使用”?”来实现互斥，表达无意义的信息之外，其他占位符都要具有具体的时间含义，且依赖关系为：年->月->日期(星期)->小时->分钟->秒数
     *
     * “30 * * * * ?” 每半分钟触发任务
     * “30 10 * * * ?” 每小时的10分30秒触发任务
     * “30 10 1 * * ?” 每天1点10分30秒触发任务
     * “30 10 1 20 * ?” 每月20号1点10分30秒触发任务
     * “30 10 1 20 10 ? *” 每年10月20号1点10分30秒触发任务
     * “30 10 1 20 10 ? 2011” 2011年10月20号1点10分30秒触发任务
     * “30 10 1 ? 10 * 2011” 2011年10月每天1点10分30秒触发任务
     * “30 10 1 ? 10 SUN 2011” 2011年10月每周日1点10分30秒触发任务
     * “15,30,45 * * * * ?” 每15秒，30秒，45秒时触发任务
     * “15-45 * * * * ?” 15到45秒内，每秒都触发任务
     * “15/5 * * * * ?” 每分钟的每15秒开始触发，每隔5秒触发一次
     * “15-30/5 * * * * ?” 每分钟的15秒到30秒之间开始触发，每隔5秒触发一次
     * “0 0/3 * * * ?” 每小时的第0分0秒开始，每三分钟触发一次
     * “0 15 10 ? * MON-FRI” 星期一到星期五的10点15分0秒触发任务
     * “0 15 10 L * ?” 每个月最后一天的10点15分0秒触发任务
     * “0 15 10 LW * ?” 每个月最后一个工作日的10点15分0秒触发任务
     * “0 15 10 ? * 5L” 每个月最后一个星期四的10点15分0秒触发任务
     * “0 15 10 ? * 5#3” 每个月第三周的星期四的10点15分0秒触发任务
     */
    @Scheduled(cron = "0 0/10 * * * *")
    public void listenerWanIp() {
        WanIpListener.getInstance().start(javaMailSender);
    }

    /**
     * 容器启动执行
     * @param args
     */
    @Override
    public void run(String... args) {
        WanIpListener.getInstance().start(javaMailSender);
    }
}
