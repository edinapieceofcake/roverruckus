package com.edinaftcrobotics.drivetrain;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class Mecanum {
    private DcMotor _frontLeft;
    private DcMotor _frontRight;
    private DcMotor _backLeft;
    private DcMotor _backRight;

    public Mecanum(DcMotor fl, DcMotor fr, DcMotor bl, DcMotor br, boolean isTeleop)
    {
        _frontLeft = fl;
        _frontRight = fr;
        _backLeft = bl;
        _backRight = br;

        if (isTeleop) {
            _backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
            _frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        }
    }

    public void SlideLeft(double power){
        _frontLeft.setPower(power);
        _frontRight.setPower(-power);
        _backLeft.setPower(-power);
        _backRight.setPower(power);
    }

    public void SlideLeft(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(distance, distance, distance, distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        SlideLeft(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        SlideLeft(0);
    }

    public void SlideRight(double power){
        _frontLeft.setPower(-power);
        _frontRight.setPower(power);
        _backLeft.setPower(power);
        _backRight.setPower(-power);
    }

    public void SlideRight(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(-distance, -distance, -distance, -distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        SlideRight(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        SlideRight(0);
    }

    public void MoveSW(double power) { // SW
        _frontLeft.setPower(power);
        _frontRight.setPower(0);
        _backLeft.setPower(0);
        _backRight.setPower(power);
    }

    public void MoveNW(double power) { // NW
        _frontLeft.setPower(0);
        _frontRight.setPower(-power);
        _backLeft.setPower(-power);
        _backRight.setPower(0);
    }

    public void MoveSE(double power) { // SE
        _frontLeft.setPower(0);
        _frontRight.setPower(power);
        _backLeft.setPower(power);
        _backRight.setPower(0);
    }

    public void MoveNE(double power) { // NE
        _frontLeft.setPower(-power);
        _frontRight.setPower(0);
        _backLeft.setPower(0);
        _backRight.setPower(-power);
    }

    public void MoveForward(double power) {
        _frontLeft.setPower(-power);
        _frontRight.setPower(-power);
        _backLeft.setPower(-power);
        _backRight.setPower(-power);
    }

    public void MoveForward(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(-distance, distance, distance, -distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        MoveForward(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        MoveForward(0);
    }

    public void MoveBackwards(double power) {
        _frontLeft.setPower(power);
        _frontRight.setPower(power);
        _backLeft.setPower(power);
        _backRight.setPower(power);
    }

    public void MoveBackwards(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(distance, -distance, -distance, distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        MoveBackwards(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        MoveBackwards(0);
    }

    public void TurnRight(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(-distance, distance, -distance, distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        TurnRight(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        MoveBackwards(0);
    }

    public void TurnRight(double power) {
        _frontLeft.setPower(-power);
        _frontRight.setPower(power);
        _backLeft.setPower(-power);
        _backRight.setPower(power);
    }

    public void TurnLeft(double power, int distance, LinearOpMode opMode) {
        StopAndResetMotors();
        SetDistance(distance, -distance, distance, -distance);

        int error = Math.abs((int)(distance * 0.95));
        int currentPosition =  Math.abs(_frontRight.getCurrentPosition());
        TurnLeft(power);

        while (_frontLeft.isBusy() && _frontRight.isBusy() && _backLeft.isBusy() && _backRight.isBusy() && (currentPosition < error)) {
            currentPosition =  Math.abs(_frontRight.getCurrentPosition());
            opMode.idle();
        }

        MoveBackwards(0);
    }

    public void TurnLeft(double power) {
        _frontLeft.setPower(power);
        _frontRight.setPower(-power);
        _backLeft.setPower(power);
        _backRight.setPower(-power);
    }

    public void Stop() {
        _frontLeft.setPower(0);
        _frontRight.setPower(0);
        _backLeft.setPower(0);
        _backRight.setPower(0);
    }

    public void Drive(double leftStickX, double leftStickY, double rightStickY) {
        final double x = Math.pow(-leftStickX, 3.0);
        final double y = Math.pow(leftStickY, 3.0);

        final double rotation = Math.pow(-rightStickY, 3.0);
        final double direction = Math.atan2(x, y);
        final double speed = Math.min(1.0, Math.sqrt(x * x + y * y));

        final double fl = speed * Math.sin(direction + Math.PI / 4.0) + rotation;
        final double fr = speed * Math.cos(direction + Math.PI / 4.0) - rotation;
        final double bl = speed * Math.cos(direction + Math.PI / 4.0) + rotation;
        final double br = speed * Math.sin(direction + Math.PI / 4.0) - rotation;

        _frontLeft.setPower(-fl);
        _frontRight.setPower(-fr);
        _backLeft.setPower(bl);
        _backRight.setPower(br);
    }

    private void StopAndResetMotors() {
        _frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        _frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        _backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        _backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    private void SetDistance(int lf, int lb, int rf, int rb) {
        _frontLeft.setTargetPosition(lf);
        _frontRight.setTargetPosition(rf);
        _backLeft.setTargetPosition(lb);
        _backRight.setTargetPosition(rb);
    }
}