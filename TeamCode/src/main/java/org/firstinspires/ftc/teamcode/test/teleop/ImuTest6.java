package org.firstinspires.ftc.teamcode.test.teleop;

import com.edinaftcrobotics.drivetrain.Mecanum;
import com.edinaftcrobotics.navigation.TurnOMatic2;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.robot.PieceOfCake;

@TeleOp(name = "Test: IMU6", group = "Teleop Test")
@Disabled
public class ImuTest6 extends LinearOpMode {
    BNO055IMU imu = null;
    Mecanum mecanum = null;

    public void runOpMode() {
        long counter = 0;
        PieceOfCake robot = new PieceOfCake();

        robot.init(hardwareMap);

        robot.getFrontL().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.getFrontR().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.getBackL().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.getBackR().setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        mecanum = new Mecanum(robot.getFrontL(), robot.getFrontR(), robot.getBackL(), robot.getBackR(), true, telemetry);

        SetupIMU();

        telemetry.addData("Waiting", "for start");
        telemetry.update();

        while (!isStarted()) {
            synchronized (this) {
                try {
                    telemetry.addData("Current Angle", "%f", GetImuAngle());
                    telemetry.update();
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        telemetry.addData("Starting", "now");
        telemetry.update();

        TurnOMatic2 turner = new TurnOMatic2(imu, mecanum, telemetry, 135, this);
        turner.Turn(.05, 3000);
    }

    private void SetupIMU() {
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu.initialize(parameters);
    }

    private double GetImuAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        return angles.firstAngle;
    }
}
