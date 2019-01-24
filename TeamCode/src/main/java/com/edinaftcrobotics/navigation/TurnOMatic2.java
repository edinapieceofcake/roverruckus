package com.edinaftcrobotics.navigation;

import com.edinaftcrobotics.drivetrain.Mecanum;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.opencv.core.Mat;

import java.util.Timer;
import java.util.TimerTask;

public class TurnOMatic2 {
    BNO055IMU imu = null;
    Mecanum mecanum = null;
    Telemetry telemetry = null;
    private double error = 0;
    private double startAngle = 0;
    private double endAngle = 0;
    private double derivative = 0;
    private double integral = 0;
    private double previousError = 0;
    private double Kp = 0.02, Ki = 0.00001, Kd = 1; // PID constant multipliers
    private double previousOutput = 0;
    private double output = 0;
    private double previousTime;
    private double firstValue = 0;
    private boolean firstRun = true;
    private LinearOpMode opMode = null;

    public TurnOMatic2(BNO055IMU imu, Mecanum mecanum, Telemetry telemetry, double toWnatAngle, LinearOpMode opMode) {
        this.imu = imu;
        this.mecanum = mecanum;
        this.telemetry = telemetry;
        startAngle = GetImuAngle();
        endAngle = toWnatAngle;
        this.opMode = opMode;
        this.mecanum.StopAndResetMotors2();
    }

    public void Turn(double closeEnough, long ticksToWait) {
        previousTime = System.currentTimeMillis();
        int inARow = 0;
        opMode.sleep(100);

        do
        {
            ComputeOuptut();
            telemetry.addData("S, C, E Angle", "%f, %f, %f", startAngle, GetImuAngle(), endAngle);
            telemetry.addData("Proportional", "%f %f",  error, Kp * error);
            telemetry.addData("Integral", "%f %f", integral, Ki * integral);
            telemetry.addData("Derivative", "%f %f", derivative, Kd * derivative);
            double left = -Range.clip(output, -1, 1) * .35;
            double right = Range.clip(output, -1, 1) * .35;
            telemetry.addData("Left Power: ", "%f", left);
            telemetry.addData("Right Power: ", "%f", right);

            opMode.sleep(200);
            mecanum.Move(left, right);
            telemetry.update();

            if (previousOutput == output) {
                inARow++;
            } else {
                inARow = 0;
            }

        } while (opMode.opModeIsActive() && (inARow != 3) && Math.abs(output) > closeEnough);

        mecanum.Stop();
    }

    private double GetImuAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double currentAngle = angles.firstAngle;

        if (currentAngle < 0)
            currentAngle += 360;
        else if (currentAngle > 180)
            currentAngle -= 360;

        return currentAngle;
    }

    private void ComputeOuptut() {
        double currentTime = System.currentTimeMillis();
        double difference = (currentTime - previousTime);
        // basic pid code for getting the angle and computing the output
        // for motor speed
        double currentAngle = GetImuAngle();

        error = endAngle - currentAngle;
        integral = integral + (error * difference);
        if (integral > 1.0) {
            integral = 1.0;
        }

        if (integral < -1.0) {
            integral = -1.0;
        }

        derivative = (error - previousError) / difference;

        previousOutput = output;
        output = (Kp * error) + (Ki * integral) + (Kd * derivative);

        previousError = error;
        previousTime = currentTime;

        telemetry.addData("Difference", difference);
    }
}
