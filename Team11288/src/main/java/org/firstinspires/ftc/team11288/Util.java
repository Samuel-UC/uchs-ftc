package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Locale;

import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER;
import static com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER;


public class Util {
    private DcMotor motorRight;
    private DcMotor motorLeft;
    private DcMotor liftMotor;
    private DcMotor armMotor;
    private Telemetry telemetry;
    private DigitalChannel touchSensor;
    private DigitalChannel liftSensor;

    private final double LIFT_MOTOR_POWER = 0.65;
    private final double ARM_MOTOR_POWER = 0.15;
    private final double DRIVE_MOTOR_POWER = 0.75;
    static final double     COUNTS_PER_MOTOR_REV    = 1250.0; //HD Hex Motor (REV-41-1301) 40:1
    static final double     COUNTS_PER_DRIVE_MOTOR_REV    = 300.0; // counts per reevaluation of the motor
    static final double INCREMENT_MOTOR_MOVE = 175.0; // move set amount at a time
    static final double INCREMENT_DRIVE_MOTOR_MOVE = 30.0; // move set amount at a time
    static final double INCHES_PER_ROTATION = 11.137; //inches per rotation of 90mm traction wheel
    static final double DEG_PER_ROTATION = 100.0; //inches per rotation of 90mm traction wheel

    public Util(DcMotor rightMotor, DcMotor leftMotor, DcMotor liftMotorIn, DcMotor armMotorIn,
                Telemetry telemetryIn, DigitalChannel touchSensorIn) {
        motorLeft = leftMotor;
        motorRight = rightMotor;
        liftMotor = liftMotorIn;
        armMotor = armMotorIn;
        telemetry = telemetryIn;
        touchSensor = touchSensorIn;
       // liftSensor = liftSensorIn;
    }

    //Drive Routines
    public void twoWheelDrive(double leftInput, double rightInput,int mode) {
        double rightDrive = scaleInput(rightInput,mode);
        double leftDrive = scaleInput(-leftInput,mode);

        double finalRight = Range.clip(rightDrive, -1, 1);
        double finalLeft = Range.clip(leftDrive, -1, 1);

        // write the values to the motors
        motorLeft.setPower(finalLeft);
        motorRight.setPower(finalRight);
    }

    void driveFixedDistance(double directionDrive, double inches, boolean isFast) {
        motorRight.setMode(STOP_AND_RESET_ENCODER);
        motorRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        motorLeft.setMode(STOP_AND_RESET_ENCODER);
        motorLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        double rotations = inches / INCHES_PER_ROTATION;
        int targetPositionR = (int) (motorRight.getCurrentPosition() + (directionDrive * rotations * COUNTS_PER_DRIVE_MOTOR_REV));
        int targetPositionL = (int) (motorLeft.getCurrentPosition() + (directionDrive * rotations * COUNTS_PER_DRIVE_MOTOR_REV));
        telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
        telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
        telemetry.update();
        while((/*touchSensor.getState() == false &&*/
                Math.abs(motorRight.getCurrentPosition() - (targetPositionR)) >INCREMENT_DRIVE_MOTOR_MOVE)
                || Math.abs(motorLeft.getCurrentPosition() - (targetPositionL)) >INCREMENT_DRIVE_MOTOR_MOVE) {
            motorRight.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            if (isFast) {
                motorRight.setPower(DRIVE_MOTOR_POWER * 2);
            } else {
                motorRight.setPower(DRIVE_MOTOR_POWER);
            }
            motorLeft.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            if (isFast) {
                motorLeft.setPower(DRIVE_MOTOR_POWER * 2);
            } else {
                motorLeft.setPower(DRIVE_MOTOR_POWER);
            }
            telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
            telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
            telemetry.update();

        }
        motorRight.setPower(0.0);
        motorRight.setMode(RUN_WITHOUT_ENCODER);
        motorRight.setDirection(DcMotorSimple.Direction.FORWARD);
        motorLeft.setPower(0.0);
        motorLeft.setMode(RUN_WITHOUT_ENCODER);
    }

    void driveAngledDistance(double directionDrive, double inches, boolean isFast) {
        double WEIGHT_R=1.2;
        motorRight.setMode(STOP_AND_RESET_ENCODER);
        motorRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorRight.setDirection(DcMotorSimple.Direction.REVERSE);
        motorLeft.setMode(STOP_AND_RESET_ENCODER);
        motorLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        double rotations = inches / INCHES_PER_ROTATION;
        int targetPositionR = (int) (motorRight.getCurrentPosition() + (directionDrive * WEIGHT_R*rotations * COUNTS_PER_DRIVE_MOTOR_REV));
        int targetPositionL = (int) (motorLeft.getCurrentPosition() + (directionDrive * rotations * COUNTS_PER_DRIVE_MOTOR_REV));
        telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
        telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
        telemetry.update();
        while((/*touchSensor.getState() == false &&*/
                Math.abs(motorRight.getCurrentPosition() - (targetPositionR)) >INCREMENT_DRIVE_MOTOR_MOVE)
                || Math.abs(motorLeft.getCurrentPosition() - (targetPositionL)) >INCREMENT_DRIVE_MOTOR_MOVE) {
            motorRight.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            if (isFast) {
                motorRight.setPower(WEIGHT_R*DRIVE_MOTOR_POWER * 2);
            } else {
                motorRight.setPower(WEIGHT_R*DRIVE_MOTOR_POWER);
            }
            motorLeft.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            if (isFast) {
                motorLeft.setPower(DRIVE_MOTOR_POWER * 2);
            } else {
                motorLeft.setPower(DRIVE_MOTOR_POWER);
            }
            telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
            telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
            telemetry.update();

        }
        motorRight.setPower(0.0);
        motorRight.setMode(RUN_WITHOUT_ENCODER);
        motorRight.setDirection(DcMotorSimple.Direction.FORWARD);
        motorLeft.setPower(0.0);
        motorLeft.setMode(RUN_WITHOUT_ENCODER);
    }



    void driveFixedDegrees(double directionDrive, double degrees) {
        motorRight.setMode(STOP_AND_RESET_ENCODER);
        motorRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motorLeft.setMode(STOP_AND_RESET_ENCODER);
        motorLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        double rotations = degrees / DEG_PER_ROTATION;
        int targetPositionR = (int) (motorRight.getCurrentPosition() + (directionDrive * rotations *
                COUNTS_PER_DRIVE_MOTOR_REV));
        int targetPositionL = (int) (motorLeft.getCurrentPosition() + (directionDrive * rotations *
                COUNTS_PER_DRIVE_MOTOR_REV));
        telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
        telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
        telemetry.update();
        while((/*touchSensor.getState() == false &&*/
                Math.abs(motorRight.getCurrentPosition() - (targetPositionR)) >INCREMENT_DRIVE_MOTOR_MOVE)
                || Math.abs(motorLeft.getCurrentPosition() - (targetPositionL)) >INCREMENT_DRIVE_MOTOR_MOVE) {
            motorRight.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            motorRight.setPower(DRIVE_MOTOR_POWER);
            motorLeft.setTargetPosition((int) (motorRight.getCurrentPosition() + (int) directionDrive*INCREMENT_DRIVE_MOTOR_MOVE));
            motorLeft.setPower(DRIVE_MOTOR_POWER);
            telemetry.addData("Drive Position R",  "= %d  %d  %d", motorRight.getCurrentPosition(),motorRight.getTargetPosition(),targetPositionR);
            telemetry.addData("Drive Position L",  "= %d  %d  %d", motorLeft.getCurrentPosition(),motorLeft.getTargetPosition(),targetPositionL);
            telemetry.update();

        }
        motorRight.setPower(0.0);
        motorRight.setMode(RUN_WITHOUT_ENCODER);
        motorLeft.setPower(0.0);
        motorLeft.setMode(RUN_WITHOUT_ENCODER);
    }
    //Lift Routines
    /*TODO:Here use the touch sensor to detect that the lift has moved to zero
    TODO  While not pressed, move lift ok
            */
    //            while(touchSensor.getState() == false){
      public void liftDrive(int direction) {
            //  if (liftSensor.getState())
            if (direction == 1) {
                liftMotor.setPower(LIFT_MOTOR_POWER);
            } else if (direction == -1) {
                liftMotor.setPower(-LIFT_MOTOR_POWER);
            } else {
                liftMotor.setPower(0);
            }
        }



    void runliftFixedDistance(double directionLift, double rotations) {
        liftMotor.setMode(STOP_AND_RESET_ENCODER);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        int targetPosition = (int) (liftMotor.getCurrentPosition() + (rotations * COUNTS_PER_MOTOR_REV));
        while((/*touchSensor.getState() == false &&*/ Math.abs(liftMotor.getCurrentPosition() - (directionLift * targetPosition)) >INCREMENT_MOTOR_MOVE)) {
            liftMotor.setTargetPosition((int) (liftMotor.getCurrentPosition() + (int) directionLift*INCREMENT_MOTOR_MOVE));
            liftMotor.setPower(directionLift * LIFT_MOTOR_POWER);
            telemetry.addData("Lift Position",  "= %d  %d  %d", liftMotor.getCurrentPosition(),liftMotor.getTargetPosition(),targetPosition);
            telemetry.update();

        }
        liftMotor.setPower(0.0);
        liftMotor.setMode(RUN_WITHOUT_ENCODER);
    }

    //Retrieval Routines
    public void servoSet(Servo myServo,double pos){
        double newPos = Range.clip(pos, -0.5, 0.5);
        myServo.setPosition(newPos);
        telemetry.addData(myServo.getDeviceName(),  "= %.2f", newPos);
        telemetry.update();
    }

    public void armDrive(int direction) {
            if(direction == 1) {
                if (touchSensor.getState()) {
                    armMotor.setPower(ARM_MOTOR_POWER);
                }
            } else if (direction == -1) {
                armMotor.setPower(-ARM_MOTOR_POWER);
            } else {
                armMotor.setPower(0);
            }
    }

    void runArmFixedDistance(double directionArm, double rotations) {
        armMotor.setMode(STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        int targetPosition = (int) (armMotor.getCurrentPosition() + (directionArm*rotations * COUNTS_PER_MOTOR_REV));
        while((Math.abs(armMotor.getCurrentPosition() - (/*directionArm * */targetPosition)) > INCREMENT_MOTOR_MOVE)) {
            armMotor.setTargetPosition((int) (armMotor.getCurrentPosition() + (int) directionArm*INCREMENT_MOTOR_MOVE));
            if ((touchSensor.getState() && directionArm == 1) || (directionArm == -1)) {
                armMotor.setPower(/*directionArm * */ARM_MOTOR_POWER);
            }
            telemetry.addData("Arm Position",  "= %d  %d  %d", armMotor.getCurrentPosition(),armMotor.getTargetPosition(),targetPosition);
            telemetry.update();

        }
        armMotor.setPower(0.0);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }


    //Sensors
    public void printColorDistance (ColorSensor sensorColor, DistanceSensor sensorDistance){
        final double SCALE_FACTOR = 255;
        float hsvValues[] = {0F, 0F, 0F};

        Color.RGBToHSV((int) (sensorColor.red() * SCALE_FACTOR),
                (int) (sensorColor.green() * SCALE_FACTOR),
                (int) (sensorColor.blue() * SCALE_FACTOR),
                hsvValues);

        // send the info back to driver station using telemetry function.
        telemetry.addData("Distance (cm)",
                String.format(Locale.US, "%.02f", sensorDistance.getDistance(DistanceUnit.CM)));
      //  telemetry.addData("Alpha", sensorColor.alpha());
      //  telemetry.addData("Red  ", sensorColor.red());
      //  telemetry.addData("Green", sensorColor.green());
      //  telemetry.addData("Blue ", sensorColor.blue());
      //  telemetry.addData("Sat", hsvValues[1]);

        telemetry.update();
    }

    //Utils
    static double scaleInput(double dVal, int mode) {
        double[] scaleArray= {};

        if(mode == 0) {
            scaleArray = new double[] {0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                    0.30, 0.36, 0.4883, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00};
        } else if(mode  == 1) {
            scaleArray = new double[]   {0.0, 0.002, 0.002, 0.006, 0.015, 0.03, 0.05, 0.08,
                    0.125, 0.17, 0.24, 0.3, 0.4, 0.5, 0.67, 0.82, 1.00};
        } else if(mode == 2) {
            scaleArray = new double[]  {0.0, 0.0, 0.003, 0.01, 0.03, 0.06, 0.1, 0.167,
                    0.25, 0.36, 0.43, 0.6499, 0.84, 1.00, 1.00, 1.00, 1.00};
        } else if(mode == 3) {
            scaleArray = new double[]{0, 0.0625, 0.125, 0.1875, 0.25, 0.3125, 0.375,
                    0.4375, 0.5, 0.5625, 0.625, 0.6875, 0.75, 0.8125, 0.875, 0.9375, 1};
        } else if(mode == 4) {
            //this mode scales down the speed except for highest power since Rev motor is higher torque
            scaleArray = new double[]  {0.0, 0.002, 0.003, 0.01, 0.03, 0.06, 0.1, 0.167,
                    0.25, 0.36, 0.43, 0.5, 0.6, 0.65, 0.7, 0.75, 1.00};
        }
        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);
        // index should be positive.
        if (index < 0) {
            index = -index;
        }
        // index cannot exceed size of array minus 1.
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