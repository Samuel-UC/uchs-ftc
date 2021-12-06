// Team11288_Teleop
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Utilities.UtilHolonomic;

/*
 * This file provides Teleop driving for the Team11288 TeleopDrive drive robot.
 * The code is structured as an Iterative OpMode
 *
 * Assumes claw with arm having shoulder motor, elbow servo and wrist servo - all having 180deg servos
 *
 */
@TeleOp(name = "TeleopDrivegoBILDA", group = "Teleop")
public class TeleopDrivegoBILDA extends OpMode {

    float gear, trigger_sensitivity = 0.1f, multiplier = 1;
    double lastLoadTime;

    private DcMotor motorFrontRight;
    private DcMotor motorFrontLeft;
    private DcMotor motorBackRight;
    private DcMotor motorBackLeft;
    private UtilHolonomic teamUtils;


    @Override
    public void init() {
        // Initialize the hardware variables.
        // Send telemetry message to signify robot waiting
        telemetry.addData("Say", "Hello Driver");

        //initialize wheels
        motorFrontRight = hardwareMap.dcMotor.get("motor front right");
        motorFrontLeft = hardwareMap.dcMotor.get("motor front left");
        motorBackLeft = hardwareMap.dcMotor.get("motor back left");
        motorBackRight = hardwareMap.dcMotor.get("motor back right");


        //utils class initializer
        teamUtils = new UtilHolonomic(motorFrontRight, motorFrontLeft, motorBackRight, motorBackLeft, telemetry);
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {

    }

    @Override
    public void loop() {
        gear = 1;
        if (gamepad1.left_trigger > trigger_sensitivity && gamepad1.right_trigger > trigger_sensitivity)
            gear = 0.5f;

        double r = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y);
        double robotAngle = Math.atan2(gamepad1.left_stick_y, -gamepad1.left_stick_x) - Math.PI / 4;

        double rightX = scaleInput(gamepad1.right_stick_x * multiplier);
        final double v1 = r * Math.cos(robotAngle) - rightX;
        final double v2 = -r * Math.sin(robotAngle) - rightX;
        final double v3 = r * Math.sin(robotAngle) - rightX;
        final double v4 = -r * Math.cos(robotAngle) - rightX;

        double FrontRight = Range.clip(v2 * multiplier * gear, -1, 1);
        double FrontLeft = Range.clip(v1 * multiplier * gear, -1, 1);
        double BackLeft = Range.clip(v3 * multiplier * gear, -1, 1);
        double BackRight = Range.clip(v4 * multiplier * gear, -1, 1);

        // write the values to the motors
        teamUtils.setWheelsToSpeedMode();
        motorFrontRight.setPower(FrontRight);
        motorFrontLeft.setPower(FrontLeft);
        motorBackLeft.setPower(BackLeft);
        motorBackRight.setPower(BackRight);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

    /*
     * This method scales the joystick input so for low joystick values, the
     * scaled value is less than linear.  This is to make it easier to drive
     * the robot more precisely at slower speeds.
     */
    double scaleInput(double dVal) {
        //original curve
        //double[] scaleArray = {0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
        //      0.30, 0.36, 0.4883, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00};

        //1/2y =1/2x^3
        //double[] scaleArray = {0.0, 0.002, 0.002, 0.006, 0.015, 0.03, 0.05, 0.08,
        // 0.125, 0.17, 0.24, 0.3, 0.4, 0.5, 0.67, 0.82, 1.00};
//1/2y = x^3
        double[] scaleArray = {0.0, 0.0, 0.003, 0.01, 0.03, 0.06, 0.1, 0.167,
                0.25, 0.36, 0.43, 0.6499, 0.84, 1.00, 1.00, 1.00, 1.00};

        // get the corresponding index for the scaleInput array.
        int index = Math.abs((int) (dVal * 16.0));
        //index cannot exceed size of array minus 1.
        if (index > 16) {
            index = 16;
        }
        // get value from the array.
        double dScale = 0.0;
        if (dVal < 0) {
            dScale = -scaleArray[index];
        } else {
            dScale = scaleArray[index];
        }
        // return scaled value.
        return dScale;
    }
}