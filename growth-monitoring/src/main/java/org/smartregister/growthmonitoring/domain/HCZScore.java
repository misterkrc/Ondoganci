package org.smartregister.growthmonitoring.domain;

import org.opensrp.api.constants.Gender;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HCZScore {
    public static double MAX_REPRESENTED_AGE = 60d;
    private final Gender gender;
    private final int month;
    private final double l;
    private final double m;
    private final double s;
    private final double sd;
    private final double sd3Neg;
    private final double sd2Neg;
    private final double sd1Neg;
    private final double sd0;
    private final double sd1;
    private final double sd2;
    private final double sd3;

    public HCZScore(Gender gender, int month, double l, double m, double s, double sd, double sd3Neg,
                    double sd2Neg, double sd1Neg, double sd0, double sd1, double sd2, double sd3) {
        this.gender = gender;
        this.month = month;
        this.l = l;
        this.m = m;
        this.s = s;
        this.sd = sd;
        this.sd3Neg = sd3Neg;
        this.sd2Neg = sd2Neg;
        this.sd1Neg = sd1Neg;
        this.sd0 = sd0;
        this.sd1 = sd1;
        this.sd2 = sd2;
        this.sd3 = sd3;
    }

    public static int getZScoreColor(final double zScore) {
        double absScore = Math.abs(zScore);
        if (absScore < 2.0) {
            return R.color.z_score_0;
        } else if (absScore >= 2.0 && absScore < 3.0) {
            return R.color.z_score_2;
        } else {
            return R.color.z_score_3;
        }
    }

    public static double roundOff(double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * This method calculates Z (The z-score) using the formulae provided here https://www.cdc.gov/growthcharts/percentile_data_files.htm
     *
     * @param x The weight to use
     * @return
     */
    public double getZ(double x) {
        if (l != 0) {
            return (Math.pow((x / m), l) - 1) / (l * s);
        } else {
            return Math.log(x / m) / s;
        }
    }

    /**
     * This method calculates X (head circumference) given the Z-Score
     *
     * @param z The z-score to use to calculate X
     * @return
     */
    public double getX(double z) {
        if (l != 0) {
            return m * Math.pow(Math.E, Math.log((z * l * s) + 1) / l);
        } else {
            return m * Math.pow(Math.E, z * s);
        }
    }

    public static Double calculate(Gender gender, Date dateOfBirth, Date measurementDate, double headCircumference) {
        if (dateOfBirth != null && gender != null && measurementDate != null) {
            int ageInMonths = (int) Math.round(getAgeInMonths(dateOfBirth, measurementDate));
            List<HCZScore> zScores = GrowthMonitoringLibrary.getInstance().hczScoreRepository().findByGender(gender);

            HCZScore zScoreToUse = null;
            for (HCZScore curZScore : zScores) {
                if (curZScore.month == ageInMonths) {
                    zScoreToUse = curZScore;
                    break;
                }
            }

            if (zScoreToUse != null) {
                return new Double(zScoreToUse.getZ(headCircumference));
            }
        }

        return null;
    }

    /**
     * This method calculates the expected head circumference given
     *
     * @param gender
     * @param ageInMonthsDouble
     * @param z
     * @return
     */
    public static Double reverse(Gender gender, double ageInMonthsDouble, Double z) {
        int ageInMonths = (int) Math.round(ageInMonthsDouble);
        List<HCZScore> zScores = GrowthMonitoringLibrary.getInstance().hczScoreRepository().findByGender(gender);

        HCZScore zScoreToUse = null;
        for (HCZScore curZScore : zScores) {
            if (curZScore.month == ageInMonths) {
                zScoreToUse = curZScore;
                break;
            }
        }

        if (zScoreToUse != null) {
            return new Double(zScoreToUse.getX(z));
        }

        return null;
    }

    public static double getAgeInMonths(Date dateOfBirth, Date measureDate) {
        Calendar dobCalendar = Calendar.getInstance();
        dobCalendar.setTime(dateOfBirth);
        standardiseCalendarDate(dobCalendar);

        Calendar measureCalendar = Calendar.getInstance();
        measureCalendar.setTime(measureDate);
        standardiseCalendarDate(measureCalendar);

        double result = 0;
        if (dobCalendar.getTimeInMillis() <= measureCalendar.getTimeInMillis()) {
            result = ((double) (measureCalendar.getTimeInMillis() - dobCalendar.getTimeInMillis())) / 2629746000l;
        }

        return result;
    }

    private static void standardiseCalendarDate(Calendar calendarDate) {
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);
    }

    public Gender getGender() {
        return gender;
    }

    public double getSd3Neg() {
        return sd3Neg;
    }

    public double getSd2Neg() {
        return sd2Neg;
    }

    public double getSd1Neg() {
        return sd1Neg;
    }

    public double getSd() { return  sd; }

    public double getSd0() {
        return sd0;
    }

    public double getSd1() {
        return sd1;
    }

    public double getSd3() {
        return sd3;
    }

    public double getSd2() {
        return sd2;
    }
}
