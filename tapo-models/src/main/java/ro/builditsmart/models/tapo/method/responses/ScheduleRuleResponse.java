package ro.builditsmart.models.tapo.method.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ro.builditsmart.models.tapo.method.requests.CountdownRuleDTO;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleRuleResponse extends TapoResponse<ScheduleRuleResponse.ScheduleRuleResult> {

    @Data
    public static class ScheduleRuleResult {
        @JsonProperty("sum")
        private int sum;
        @JsonProperty("enable")
        private boolean enable;
        @JsonProperty("start_index")
        private int startIndex;
        @JsonProperty("schedule_rule_max_count")
        private int scheduleRuleMaxCount;
        @JsonProperty("rule_list")
        private List<ScheduleRule> ruleList = new ArrayList<>();
    }

    @Data
    public static class ScheduleRule {

        @JsonProperty("day")
        private Integer day;

        @JsonProperty("desired_states")
        private CountdownRuleDTO.DesiredStates desiredStates;

        @JsonProperty("e_action")
        private String endAction;

        @JsonProperty("e_min")
        private int endTimeMin;

        @JsonProperty("e_type")
        private RuleTimeType endTimeType;

        @JsonProperty("enable")
        private boolean enable;

        @JsonProperty("id")
        private String id;

        @JsonProperty("mode")
        private RuleMode mode;

        @JsonProperty("month")
        private int month;

        @JsonProperty("s_min")
        private int startTimeMin;

        @JsonProperty("s_type")
        private RuleTimeType startTimeType;

        @JsonProperty("time_offset")
        private int timeOffset;

        @JsonProperty("week_day")
        private byte weekOfDays;

        @JsonProperty("year")
        private int year;

    }

    public enum RuleMode {
        once, repeat
    }

    public enum RuleTimeType {
        none,
        normal,
        sunrise,
        sunset
    }



}
