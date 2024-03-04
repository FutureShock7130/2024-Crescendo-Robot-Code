package frc.robot.commands;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.ChenryLib.PID;
import frc.robot.Constants.UpperConstants;
import frc.robot.Constants.UpperState;
import frc.robot.subsystems.UpperSub;

public class AutoUpper extends Command{
    private final UpperSub s_Upper;

    private UpperState state;
    
    private double elbowAngle;
    private double intakeSpeed;
    private double shooterSpeed;

    private double time, lastTime;

    private final PID elbowPID = new PID(
        UpperConstants.elbowKP, 
        UpperConstants.elbowKI,
        UpperConstants.elbowKD,
        UpperConstants.elbowiWindup,
        UpperConstants.elbowiLimit
    );

    public AutoUpper(UpperSub s_Upper, UpperState state) {
        this.s_Upper = s_Upper;
        this.state = state;
        addRequirements(s_Upper);
    }

    public AutoUpper(UpperSub s_Upper, double elbowAngle, double shooterSpeed, double intakeSpeed, double time) {
        this.s_Upper = s_Upper;
        this.elbowAngle = elbowAngle;
        this.shooterSpeed = shooterSpeed;
        this.intakeSpeed = intakeSpeed;
        this.time = time;
    }

    @Override
    public void initialize() {
        System.out.println("AutoUpper.initialize()");
        lastTime = Timer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        if(state != null) {
            switch (state) {
                case GROUND:
                    elbowAngle = UpperConstants.ELBOW_GROUND_POS;
                    intakeSpeed = s_Upper.hasNote() ? 0: UpperConstants.INTAKE_GROUND_SPEED;
                    shooterSpeed = UpperConstants.SHOOTER_GROUND_SPEED;
                    if(s_Upper.hasNote()) s_Upper.setLED(12,41,235);
                    else s_Upper.blink(12,41,235);
                    if(s_Upper.hasNote()) state = UpperState.DEFAULT;
                    break;
                case SPEAKER:
                    elbowAngle = UpperConstants.ELBOW_SPEAKER_POS;
                    intakeSpeed = 0;
                    shooterSpeed = UpperConstants.SHOOTER_SHOOT_SPEED;
                    if(Math.abs(s_Upper.getShooterRPM()) > 5000) s_Upper.setLED(255,0,0);
                    else s_Upper.setLED(0,255,0);
                    break;
                case SHOOT:
                    intakeSpeed = UpperConstants.INTAKE_SHOOT_SPEED;
                    shooterSpeed = UpperConstants.SHOOTER_SHOOT_SPEED;
                    s_Upper.blink(255,0,0);
                    break;
                default:
                    break;
            }
        }

        s_Upper.setElbow(-elbowPID.calculate(elbowAngle - s_Upper.getElbowRotation()));
        s_Upper.setShooter(shooterSpeed);
        s_Upper.setIntake(intakeSpeed);
    }

    @Override
    public void end(boolean interrupted) {
        s_Upper.setElbow(0);
        s_Upper.setShooter(0);
        s_Upper.setIntake(0);
        System.out.println("AutoUpper.end()");
    }

    @Override
    public boolean isFinished() {
        if(state != null) {
            if(Math.abs(elbowAngle - s_Upper.getElbowRotation()) < 0.005) return true;
        } else {
            if(Timer.getFPGATimestamp() - lastTime > time) return true;
        }
        return false;
    }
}
