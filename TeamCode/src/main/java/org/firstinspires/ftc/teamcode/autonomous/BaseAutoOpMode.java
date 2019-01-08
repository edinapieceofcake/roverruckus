package org.firstinspires.ftc.teamcode.autonomous;

import com.edinaftcrobotics.drivetrain.Mecanum;
import com.edinaftcrobotics.navigation.TurnOMatic;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.enums.AutonomousStates;
import org.firstinspires.ftc.teamcode.enums.MineralLocation;
import org.firstinspires.ftc.teamcode.robot.PieceOfCake;

import java.util.List;

abstract class BaseAutoOpMode extends LinearOpMode {
    protected int DrivePerInch = (int)(1120 / 18.85);
    private int FlatFlip = 1800;
    private int SlideOffLatchDistance = 275;
    private int Turn90 = 1325;
    private int Turn45 = Turn90/2;
    private ElapsedTime watch = new ElapsedTime();
    protected double PushMineralDistance = 4.5;
    protected double BackAwayFromMineralDistance = 5.5;
    protected int slideRightPosition = DrivePerInch * 20;
    protected int slideLeftPosition = DrivePerInch * 23;
    protected int driveForwardPosition = (int)(DrivePerInch * 20.5);

    private static final String VUFORIA_KEY = "ASA9XvT/////AAABmUnq30r9sU3Nmf/+RS+Xx0CHgJj/JtD5ycahnuM/0B2SFvbMRPIZCbLi4LeOkfse9Dymor5W7vNMYI+vmqVx9kpEaKE8VM7cFMUb/T1LLwlCPdX9QKOruzTcRdlYswR7ULh4K11GuFZDO/45pSks+Nf25kT5cnV+IN3TsscA0o7I6XPIeUoAJJPsjw+AycsmRk2uffr3Bnupexr93iRfHylniqP+ss4cRcT1lOqS5Zhh7FQaoelR58qL/RUorGpknjy9ufCn9ervc6Mz01u3ZkM/EOa5wUPT8bDzPZ6nMDaadqumorT5Py+GtJSUosUgz4Gd3iR++fdEk6faFZq3L9xfBSagNykwhiyYx+oqwVqe";
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    protected MineralLocation mineralLocation = MineralLocation.RIGHT;
    private VuforiaLocalizer vuforia;

    protected PieceOfCake robot = new PieceOfCake();
    protected Mecanum mecanum = null;

    protected BNO055IMU imu = null;
    protected TFObjectDetector tfod;
    protected Recognition LastRecognition = null;

    protected void InitRobot() {
        robot.init(hardwareMap);

        mecanum = new Mecanum(robot.getFrontL(), robot.getFrontR(), robot.getBackL(), robot.getBackR(), true, telemetry);

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        if (tfod != null) {
            tfod.activate();
        }
    }

    protected void InitGyro() {
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu.initialize(parameters);
        while (!imu.isGyroCalibrated());
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
        mineralLocation = MineralLocation.RIGHT;
    }

    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    public void LocateTFMineral() {
        if (tfod != null) {
            // getUpdatedRecognitions() will return null if no new information is available since
            // the last time that call was made.
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if ((updatedRecognitions != null) && (updatedRecognitions.size() > 0)) {
                mineralLocation = MineralLocation.RIGHT;
                for (Recognition recognition : updatedRecognitions) {
                    telemetry.addData("Object", recognition);
                    if ((recognition.getLabel().equals(LABEL_GOLD_MINERAL)) && (recognition.getTop() > 520) && (recognition.getBottom() < 730)) {
                        int goldMineralX = (int) recognition.getLeft();
                        if (goldMineralX < 430) {
                            mineralLocation = MineralLocation.LEFT;
                        } else if ((goldMineralX >= 430)  && (goldMineralX <= 650)) {
                            mineralLocation = MineralLocation.MIDDLE;
                        } else {
                            mineralLocation = MineralLocation.RIGHT;
                        }

                        LastRecognition = recognition;

                        break;
                    }
                }
            } else {
                telemetry.addData("Nothing New", "Detected");
            }
        } else {
            telemetry.addData("No", "TFOD");
        }
    }

    public void ShutdownTFOD() {
        if (tfod != null) {
            tfod.shutdown();
        }
    }

    public AutonomousStates Latch() {
        robot.getBackLift().setPower(-.12);
        robot.getFrontLift().setPower(.12);

        return AutonomousStates.LATCHED;
    }

    public AutonomousStates MoveToLeftWall(int distanceFromLeftMineral, int distanceFromCenterMineral, int distanceFromRightMineral) {
        if (mineralLocation == MineralLocation.RIGHT) {
            mecanum.SlideLeft(0.5, distanceFromRightMineral, this);
        } else if (mineralLocation == MineralLocation.LEFT) {
            mecanum.SlideLeft(0.5, distanceFromLeftMineral, this);
        } else if (mineralLocation == MineralLocation.MIDDLE) {
            mecanum.SlideLeft(0.5, distanceFromCenterMineral, this);
        }

        robot.getFrontFlip().setTargetPosition(800);
        robot.getFrontFlip().setPower(.7);

        while ((robot.getFrontFlip().isBusy()) && opModeIsActive()) {
            idle();
        }

        return AutonomousStates.AT_LEFT_WALL;
    }

    public AutonomousStates Drop() {
        robot.getSlide().setPower(.1);
        // do something to drop
        robot.getTopFlip().setPosition(1);
        robot.getFrontFlip().setTargetPosition(FlatFlip);
        robot.getFrontFlip().setPower(.7);

        while (robot.getFrontFlip().isBusy() && opModeIsActive()) {
            idle();
        }

        //robot.getLockServo().setPower(-1);
        robot.getBackLift().setPower(0);
        robot.getFrontLift().setPower(0);

        watch.reset();
        while ((watch.milliseconds() < 1500) && opModeIsActive()) {
            idle();
        }

        robot.getBackLift().setPower(.3);
        robot.getFrontLift().setPower(-.3);
        watch.reset();
        while ((watch.milliseconds() < 400) && opModeIsActive()) {
            idle();
        }

        robot.getBackLift().setPower(0);
        robot.getFrontLift().setPower(0);

        return AutonomousStates.DROPPED;
    }

    public AutonomousStates MoveLeftOffLatch() {
        robot.getTopFlip().setPosition(1);

        mecanum.SlideLeft2(.7, SlideOffLatchDistance, this);

        mecanum.MoveBackwards(.3, 50, this);

        return AutonomousStates.MOVED_OFF_LATCH;
    }
    
    public AutonomousStates MoveForward(int forwardDistance) {
        robot.getSlide().setPower(.5);

        mecanum.MoveForward2(.7, forwardDistance, this);

        robot.getSlide().setPower(0);

        return AutonomousStates.MOVED_FORWARD;
    }

    public AutonomousStates MoveForwardAndSlideBackToCenter(int forwardDistance) {
        robot.getSlide().setPower(.5);

        mecanum.MoveForward2(.6, forwardDistance, this);
        mecanum.SlideRight2(.7, SlideOffLatchDistance, this);

        robot.getSlide().setPower(0);

        return AutonomousStates.MOVED_BACK_TO_CENTER;
    }

    public AutonomousStates DriveToMineral(int slideLeftDistance, int slideRightDistance) {
        if (mineralLocation == MineralLocation.LEFT) {
            mecanum.SlideLeft2(.5, slideLeftDistance, this);
        } else if (mineralLocation == MineralLocation.RIGHT) {
            mecanum.SlideRight2(.5, slideRightDistance, this);
        }

        return AutonomousStates.AT_MINERAL;
    }

    public AutonomousStates DriveToMineralOffLeftOffset(int slideLeftDistance, int slideRightDistance) {
        robot.getSlide().setPower(.1);

        if (mineralLocation == MineralLocation.LEFT) {
            mecanum.SlideLeft(.5, slideLeftDistance - SlideOffLatchDistance, this);
        } else if (mineralLocation == MineralLocation.RIGHT) {
            mecanum.SlideRight(.5, slideRightDistance + SlideOffLatchDistance, this);
        } else {
            mecanum.SlideRight(.5, SlideOffLatchDistance, this);
        }

        robot.getSlide().setPower(0);

        return AutonomousStates.AT_MINERAL;
    }

    public AutonomousStates PushMineral (int pushDistance) {
        robot.getSlide().setPower(.1);
        mecanum.MoveForward2(.7, pushDistance, this);
        robot.getSlide().setPower(0);

        return AutonomousStates.MINERAL_PUSHED;
    }

    public AutonomousStates BackAwayFromMineral(int backDistance) {

        robot.getSlide().setPower(.1);
        mecanum.MoveBackwards2(.7, backDistance, this);
        robot.getSlide().setPower(0);

        return AutonomousStates.BACKED_AWAY_FROM_MINERAL;
    }

    public AutonomousStates ExtendArm() {
        watch.reset();

        robot.getFrontFlip().setTargetPosition(FlatFlip);
        robot.getFrontFlip().setPower(.7);

        // slide arm out
        robot.getSlide().setPower(-1);
        // run for 1000 milliseconds
        while ((watch.milliseconds() < 1000) && opModeIsActive()) {
            idle();
        }

        robot.getSlide().setPower(0);

        return AutonomousStates.ARM_EXTENDED;
    }

    public AutonomousStates DropMarker() {
        watch.reset();
        // slide arm out
        robot.getSlide().setPower(-1);
        // run for 1000 milliseconds
        while ((watch.milliseconds() < 1000) && opModeIsActive()) {
            idle();
        }

        robot.getSlide().setPower(0);

        // spin the intake to dump marker
        robot.getIntake().setPower(-1);
        watch.reset();
        while ((watch.milliseconds() < 2000) && opModeIsActive()) {
            idle();
        }

        robot.getIntake().setPower(0);

        // move slide back in
        watch.reset();
        robot.getSlide().setPower(1);
        // run for 1000 milliseconds
        while ((watch.milliseconds() < 1000) && opModeIsActive()) {
            idle();
        }

        robot.getSlide().setPower(0);

        return AutonomousStates.DROPPED_MARKER;
    }

    public AutonomousStates TurnLeftTowardsCraterOrDepot() {
        mecanum.TurnLeft(.5, Turn45 + Turn90, this);

        mecanum.SlideRight(.5, DrivePerInch * 15, this);

        return AutonomousStates.TURNED_TOWARDS_DEPOT_OR_CRATER;
    }

    public AutonomousStates TurnTowardsCraterFromDepot() {
        mecanum.MoveBackwards2(.5,DrivePerInch * 9, this);

        mecanum.TurnLeft(.5, Turn90 + Turn90, this);

        return AutonomousStates.FACING_CRATER;
    }

    public AutonomousStates MoveTowardsCrater() {
        mecanum.MoveForward(.3, DrivePerInch * 12, this);

        return AutonomousStates.AT_CRATER;
    }

    public AutonomousStates MoveTowardsDepot(){
        mecanum.MoveForward(.5,DrivePerInch * 9, this);

        return AutonomousStates.AT_DEPOT;
    }
}
