package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.Joystick;

public class Elevator
{
    int masterElevatorMotorID = 0;
    int slaveElevatorMotorID = 1;
    State currentState = State.Off; 
    private WPI_TalonSRX masterElevatorMotor = new WPI_TalonSRX(masterElevatorMotorID);
    private WPI_VictorSPX slaveElevatorMotor = new WPI_VictorSPX(slaveElevatorMotorID);

    public static enum State
    {
        Off(0.0)
        {
        void doAction(WPI_TalonSRX motorController, double speed){
            System.out.println("Testing Off");
            motorController.set(ControlMode.PercentOutput, speed);
        }
        },
        Holding(0.05)
        {
        void doAction(WPI_TalonSRX motorController, double speed){
            System.out.println("Testing Holding");
            speed = 0.05; 
            motorController.set(ControlMode.PercentOutput, speed);
        }
        },
        MovingUp(.5)
        {
        void doAction(WPI_TalonSRX motorController, double speed){
            System.out.println("Testing Moving Up");
            motorController.set(ControlMode.PercentOutput, speed);
        }
        },
        MovingDown(-.5)
        {
            void doAction(WPI_TalonSRX motorController, double speed){
                System.out.println("Testing Moving Down");
                motorController.set(ControlMode.PercentOutput, speed);
            }

        };

        abstract void doAction(WPI_TalonSRX motorController, double speed);

        private final double speed;
    
        State(double speed)
        {
          this.speed = speed;
        }
    
        public double speed()
        {
          return speed;
        }
    }

    private static enum Event
    {  
        ElevatorUpButtonPressed, 
        ElevatorDownButtonPressed, 
        ElevatorHoldingButtonPressed,
        NothingPressed
    }

    public static enum Transition
    {
     // name          current state                 event                      new state 
        TRANSITION_01 (State.Off,          Event.ElevatorUpButtonPressed,       State.MovingUp),
        TRANSITION_02 (State.Off,          Event.ElevatorDownButtonPressed,     State.MovingDown),
        TRANSITION_03 (State.Off,          Event.ElevatorHoldingButtonPressed,  State.Holding),
        TRANSITION_04 (State.Off,          Event.NothingPressed,                State.Off),
        TRANSITION_05 (State.MovingUp,     Event.ElevatorUpButtonPressed,       State.MovingUp),
        TRANSITION_06 (State.MovingUp,     Event.ElevatorDownButtonPressed,     State.MovingDown),
        TRANSITION_07 (State.MovingUp,     Event.ElevatorHoldingButtonPressed,  State.Holding),
        TRANSITION_08 (State.MovingUp,     Event.NothingPressed,                State.Off),
        TRANSITION_09 (State.MovingDown,   Event.ElevatorUpButtonPressed,       State.MovingUp),
        TRANSITION_10 (State.MovingDown,   Event.ElevatorDownButtonPressed,     State.MovingDown),
        TRANSITION_11 (State.MovingDown,   Event.ElevatorHoldingButtonPressed,  State.Holding),
        TRANSITION_12 (State.MovingDown,   Event.NothingPressed,                State.Off),
        TRANSITION_13 (State.Holding,      Event.ElevatorUpButtonPressed,       State.MovingUp),
        TRANSITION_14 (State.Holding,      Event.ElevatorDownButtonPressed,     State.MovingDown),
        TRANSITION_15 (State.Holding,      Event.ElevatorHoldingButtonPressed,  State.Holding),
        TRANSITION_16 (State.Holding,      Event.NothingPressed,                State.Off);
        
      private final State currentState;
      private final Event event;
      private final State nextState;
      
      Transition(State currentState, Event event, State nextState)
      {
        this.currentState = currentState;
        this.event = event;
        this.nextState = nextState;
      }
  
      // table lookup to determine new state given the current state and the event
      public static State findNextState (State currentState, Event event)
      {
        for (Transition transition : Transition.values())
        {
          if (transition.currentState == currentState && transition.event == event) 
          {
              return transition.nextState;
          }
        }
        return currentState; // throw an error if here
      }
    }

    private Elevator()
    {
        System.out.print("Initializing the Elevator");
        
        slaveElevatorMotor.follow(masterElevatorMotor);
        System.out.println("Done Initializing the Elevator");
    }
    
    private static Elevator elevatorInstance = new Elevator();
    public static Elevator getElevatorInstance()
    {
        return elevatorInstance;
    }

    private boolean[] getButtonArray()
    {
        Joystick joystick = new Joystick(0);
        int[] actuatedButtonIdx  = {0, 1, 2 }; // stores the button numbers that correspond to targetPosition values
        boolean[] buttonArray = {false, false, false}; // initial values of buttons - same order as actuatedButtonIdx
        boolean buttonDown = false;
    
        for (int idx=0; idx<actuatedButtonIdx.length; idx++)
        buttonArray[idx] = joystick.getRawButton(actuatedButtonIdx[idx]);
    
        return buttonArray;
    }
    
    public void ElevatorFSM()
    { 
        boolean[] buttonArray = getButtonArray();
        Event event;
        //TODO: need to verify what buttons are with what index
        if (buttonArray[0])
        {
            event = Event.ElevatorUpButtonPressed;
        }
        else if(buttonArray[1])
        {
            event = Event.ElevatorDownButtonPressed;
        }
        else if(buttonArray[2])
        {
            event = Event.ElevatorHoldingButtonPressed;
        }
        else
        {
            event = Event.NothingPressed;
        }

        currentState  = Transition.findNextState (currentState, event);
        currentState.doAction(masterElevatorMotor, currentState.speed());
    }


}
