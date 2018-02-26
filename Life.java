import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.BROWN;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.PINK;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.YELLOW;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Callable;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class Life extends Application
{

	private ObservableList<Thing>	balls		= FXCollections.observableArrayList();
	private static final int		NUM_BALLS	= 100000;
	private static final double		MIN_RADIUS	= 3;
	private static final double		MAX_RADIUS	= 3;
	private static final double		MIN_SPEED	= 20;
	private static final double		MAX_SPEED	= 100;
 
	private final FrameStats		frameStats	= new FrameStats();

	@Override
	public void start(Stage primaryStage)
	{
		final Pane ballContainer = new Pane();

		constrainBallsOnResize(ballContainer);

		ballContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					balls.clear();
					createBalls(NUM_BALLS, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, ballContainer.getWidth(),
							ballContainer.getHeight());
				}
			}
		});

		balls.addListener(new ListChangeListener<Thing>()
		{
			@Override
			public void onChanged(Change<? extends Thing> change)
			{
				while (change.next())
				{
					for (Thing b : change.getAddedSubList())
					{
						ballContainer.getChildren().add(b.getView());
					}
					for (Thing b : change.getRemoved())
					{
				ballContainer.getChildren().remove(b.getView());
					}
				}
			}
		});

		
		createBalls(NUM_BALLS, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, ballContainer.getWidth(),
				ballContainer.getHeight());

		final BorderPane root = new BorderPane();
		final Label stats = new Label();
		stats.textProperty().bind(frameStats.textProperty());

		root.setCenter(ballContainer);
		root.setBottom(stats);

		final Scene scene = new Scene(root, 1000	, 1000);
		primaryStage.setScene(scene);
		primaryStage.show();

		startAnimation(ballContainer);
	}

	private void startAnimation(final Pane ballContainer)
	{
		final LongProperty lastUpdateTime = new SimpleLongProperty(0);
		final AnimationTimer timer = new AnimationTimer()
		{
			@Override
			public void handle(long timestamp)
			{
				if (lastUpdateTime.get() > 0)
				{
					long elapsedTime = timestamp - lastUpdateTime.get();
					checkCollisions(ballContainer.getWidth(), ballContainer.getHeight());

					updateWorld(elapsedTime);
					frameStats.addFrame(elapsedTime);
				}
				lastUpdateTime.set(timestamp);
			}

		};
		timer.start();
	}

	// setting new values for every element

	private void updateWorld(long elapsedTime)
	{
		double elapsedSeconds = elapsedTime / 1000000000.0;
		for (Thing b : balls)
		{

			
			//b.getView().setFill(Color.hsb((b.getSpeed()/MAX_SPEED)*70-150, 1, 1));
			b.setCenterX(b.getCenterX() + elapsedSeconds * b.getXVelocity());
			b.setCenterY(b.getCenterY() + elapsedSeconds * b.getYVelocity());
		}
	}

	private static class Grid
	{
		ArrayList<Thing>	things	= new ArrayList<Thing>();
		Thing[][][]			a;

		public Grid(int length, int height)
		{

			a = new Thing[height + 100][length + 100][20];

		}

		public void addThing(int x, int y, Thing t)
		{
			
			int loc = 0;
			if (x<0)
			{
				x=0;
			}
			if(y<0)
			{
				y=0;
			}

			//System.out.println(x+","+y);
			for (int z = 0; a[y][x][z] != null && z < 10; z++)
			{
				loc++;
			}
			//System.out.println(x+",,,"+y);
			a[y][x][loc] = t;
		}

		public Thing getThing(int x, int y, int z)
		{
			return a[y][x][z];

		}

	}

	// checking if there are any collisions and setting new speed values.
	private void checkCollisions(double maxX, double maxY)
	{

		int gSize = (int) (1.5 * (MAX_RADIUS + MIN_RADIUS));
		int gSizeX = (int) (maxX / (gSize));

		int gSizeY = (int) (maxY / (gSize));
//System.out.println(gSizeX+","+gSizeY);
		Grid grid = new Grid(gSizeY, gSizeX);
//System.out.println("new");
		for (ListIterator<Thing> slowIt = balls.listIterator(); slowIt.hasNext();)
		{

			Thing b1 = slowIt.next();

			// check wall collisions:
			double xVel = b1.getXVelocity();
			double yVel = b1.getYVelocity();
			if ((b1.getCenterX() - b1.getRadius() <= 0 && xVel < 0)
					|| (b1.getCenterX() + b1.getRadius() >= maxX && xVel > 0))
			{
				b1.setXVelocity(-xVel);
			}
			if ((b1.getCenterY() - b1.getRadius() <= 0 && yVel < 0)
					|| (b1.getCenterY() + b1.getRadius() >= maxY && yVel > 0))
			{
				b1.setYVelocity(-yVel);
			}
			double radius = b1.getRadius();
			int x1 = (int) (b1.getCenterX() - radius);
			int x2 = (int) (b1.getCenterX() + radius);
			int y1 = (int) (b1.getCenterY() - radius);
			int y2 = (int) (b1.getCenterY() + radius);//work out on paper
			
			if (x1 / gSize != x2 / gSize && y1 / gSize != y2 / gSize)// if one side
			{
	//			System.out.println("\n"+y1+","+y2  );
				grid.addThing(x2 / gSize, y2 / gSize, b1);

				grid.addThing(x1 / gSize, y2 / gSize, b1);
				grid.addThing(x2 / gSize, y1 / gSize, b1);
				grid.addThing(x1 / gSize, y1 / gSize, b1);
				continue;
			} else if (x1 / gSize != x2 / gSize && y1 / gSize == y2 / gSize)
			{
		//		System.out.println("\n"+y1+","+y2  );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				grid.addThing(x1 / gSize, y2 / gSize, b1);
				continue;
			} else if (x1 / gSize == x2 / gSize && y1 / gSize != y2 / gSize)
			{
			//	System.out.println("\n"+y1+","+y2  );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				grid.addThing(x2 / gSize, y1 / gSize, b1);
				continue;
			} else
			{
				//System.out.println("\n"+y1+","+y2  );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				continue;
			}

			// for (ListIterator<Thing> fastIt = balls.listIterator(slowIt.nextIndex());
			// fastIt.hasNext();)
			// {
			// Thing b2 = fastIt.next();
			// // performance hack: both colliding(...) and bounce(...) need deltaX and
			// deltaY,
			// // so compute them once here:
			// final double deltaX = b2.getCenterX() - b1.getCenterX();
			// final double deltaY = b2.getCenterY() - b1.getCenterY();
			// if (colliding(b1, b2, deltaX, deltaY))
			// {
			// bounce(b1, b2, deltaX, deltaY);
			// }
			// }
		}
		for (int y = 0; y < gSizeY+1; y++)
		{
			for (int x = 0; x < gSizeX+1; x++)
			{

				for (int z = 0; z < 18; z++)
				{

					if (grid.a[y][x][z] == null)
					{	
						
						break;
					} else if (grid.a[y][x][1] == null)
					{

						break;
					} else
					{

						for (int k = z + 1; grid.a[y][x][k] != null; k++)
						{
						
							
							final double deltaX = grid.getThing(x, y, z).getCenterX()
									- grid.getThing(x, y, k).getCenterX();
							final double deltaY = grid.getThing(x, y, z).getCenterY()
									- grid.getThing(x, y, k).getCenterY();
					//		System.out.println(grid.getThing(x, y, z).getCenterX()+", "+grid.getThing(x, y, k).getCenterX());
							if (deltaY == 0 && deltaX == 0)
							{
								break;
							}

							if (colliding(grid.getThing(x, y, z), grid.getThing(x, y, k), deltaX, deltaY))
							{
								bounce(grid.getThing(x, y, z), grid.getThing(x, y, k), deltaX, deltaY);
							}
						}

					}
				}

			}
		}
	}

	// checking if two balls are colliding
	public boolean colliding(final Thing b1, final Thing b2, final double deltaX, final double deltaY)
	{
		// square of distance between balls is s^2 = (x2-x1)^2 + (y2-y1)^2
		// balls are "overlapping" if s^2 < (r1 + r2)^2
		// We also check that distance is decreasing, i.e.
		// d/dt(s^2) < 0:
		// 2(x2-x1)(x2'-x1') + 2(y2-y1)(y2'-y1') < 0

		final double radiusSum = b1.getRadius() + b2.getRadius();
		if (deltaX * deltaX + deltaY * deltaY <= radiusSum * radiusSum)
		{
			if (deltaX * (b2.getXVelocity() - b1.getXVelocity()) + deltaY * (b2.getYVelocity() - b1.getYVelocity()) > 0)
			{

				return true;
			}
		}
		return false;
	}

	private void bounce(final Thing b1, final Thing b2, final double deltaX, final double deltaY)
	{
		final double distance = sqrt(deltaX * deltaX + deltaY * deltaY);
		final double unitContactX = deltaX / distance;
		final double unitContactY = deltaY / distance;

		final double xVelocity1 = b1.getXVelocity();
		final double yVelocity1 = b1.getYVelocity();
		final double xVelocity2 = b2.getXVelocity();
		final double yVelocity2 = b2.getYVelocity();

		final double u1 = xVelocity1 * unitContactX + yVelocity1 * unitContactY; // velocity of ball 1 parallel to
																					// contact vector
		final double u2 = xVelocity2 * unitContactX + yVelocity2 * unitContactY; // same for ball 2

		final double massSum = b1.getMass() + b2.getMass();
		final double massDiff = b1.getMass() - b2.getMass();

		final double v1 = (2 * b2.getMass() * u2 + u1 * massDiff) / massSum; // These equations are derived for
																				// one-dimensional collision by
		final double v2 = (2 * b1.getMass() * u1 - u2 * massDiff) / massSum; // solving equations for conservation of
																				// momentum and conservation of energy

		final double u1PerpX = xVelocity1 - u1 * unitContactX; // Components of ball 1 velocity in direction perpendicular
		final double u1PerpY = yVelocity1 - u1 * unitContactY; //to contact vector. This doesn't change with collision
		final double u2PerpX = xVelocity2 - u2 * unitContactX; // Same for ball 2....
		final double u2PerpY = yVelocity2 - u2 * unitContactY;

		b1.setXVelocity(v1 * unitContactX + u1PerpX);
		b1.setYVelocity(v1 * unitContactY + u1PerpY);
		b2.setXVelocity(v2 * unitContactX + u2PerpX);
		b2.setYVelocity(v2 * unitContactY + u2PerpY);

	}

	private void createBalls(int numBalls, double minRadius, double maxRadius, double minSpeed, double maxSpeed,
			double initialX, double initialY)
	{
		final Random rng = new Random();
		for (int i = 0; i < numBalls; i++)
		{
			double radius = minRadius + (maxRadius - minRadius) * rng.nextDouble();
			double mass = Math.pow((radius / 40), 3);

			final double speed = minSpeed + (maxSpeed - minSpeed) * rng.nextDouble();
			final double angle = Math.random()*360;
			Thing ball = new Thing(Math.random() * initialX/2+initialX/5, Math.random() * initialY/1.2+initialY/10, radius, speed * cos(angle),
					speed * sin(angle), mass);
			ball.getView().setFill(Color.hsb(speed, 1, 1));
			// ball.getView().setFill(i==0 ? RED : TRANSPARENT);
			balls.add(ball);
		}
	}

	private void constrainBallsOnResize(final Pane ballContainer)
	{
		ballContainer.widthProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if (newValue.doubleValue() < oldValue.doubleValue())
				{
					for (Thing b : balls)
					{
						double max = newValue.doubleValue() - b.getRadius();
						if (b.getCenterX() > max)
						{
							b.setCenterX(max);
						}
					}
				}
			}
		});

		ballContainer.heightProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if (newValue.doubleValue() < oldValue.doubleValue())
				{
					for (Thing b : balls)
					{
						double max = newValue.doubleValue() - b.getRadius();
						if (b.getCenterY() > max)
						{
							b.setCenterY(max);
						}
					}
				}
			}

		});
	}

	private static class Thing
	{
		private final DoubleProperty		xVelocity;	// pixels per second
		private final DoubleProperty		yVelocity;
		private final ReadOnlyDoubleWrapper	speed;
		private final double				mass;		// arbitrary units
		private final double				radius;		// pixels

		private final Circle				view;

		public Thing(double centerX, double centerY, double radius, double xVelocity, double yVelocity, double mass)
		{

			this.view = new Circle(centerX, centerY, radius);
			this.xVelocity = new SimpleDoubleProperty(this, "xVelocity", xVelocity);
			this.yVelocity = new SimpleDoubleProperty(this, "yVelocity", yVelocity);
			this.speed = new ReadOnlyDoubleWrapper(this, "speed");
			speed.bind(Bindings.createDoubleBinding(new Callable<Double>()
			{

				@Override
				public Double call() throws Exception
				{

					final double xVel = getXVelocity();
					final double yVel = getYVelocity();
					return sqrt(xVel * xVel + yVel * yVel);
				}
			}, this.xVelocity, this.yVelocity));
			this.mass = mass;
			this.radius = radius;
			view.setRadius(radius);
		}

		public double getMass()
		{
			return mass;
		}

		public double getRadius()
		{
			return radius;
		}

		public final double getXVelocity()
		{
			return xVelocity.get();
		}

		public final void setXVelocity(double xVelocity)
		{
			this.xVelocity.set(xVelocity);
		}


		public final DoubleProperty xVelocityProperty()
		{
			return xVelocity;
		}

		public final double getYVelocity()
		{
			return yVelocity.get();
		}

		public final void setYVelocity(double yVelocity)
		{
			this.yVelocity.set(yVelocity);
		}

		public final DoubleProperty yVelocityProperty()
		{
			return yVelocity;
		}

		public final double getSpeed()
		{
			return speed.get();
		}

		public final ReadOnlyDoubleProperty speedProperty()
		{
			return speed.getReadOnlyProperty();
		}

		public final double getCenterX()
		{
			return view.getCenterX();
		}

		public final void setCenterX(double centerX)
		{
			view.setCenterX(centerX);
		}

		public final DoubleProperty centerXProperty()
		{
			return view.centerXProperty();
		}

		public final double getCenterY()
		{
			return view.getCenterY();
		}

		public final void setCenterY(double centerY)
		{
			view.setCenterY(centerY);
		}

		public final DoubleProperty centerYProperty()
		{
			return view.centerYProperty();
		}

		public Shape getView()
		{
			return view;
		}
	}

	private static class Area
	{
		private final Rectangle view;

		public Area(double height, double width)
		{
			this.view = new Rectangle(width, height);

		}

		public final double getWidth()
		{
			return view.getWidth();
		}

		public final void setWidth(double centerX)
		{
			view.setWidth(centerX);
		}

		public final DoubleProperty centerXProperty()
		{
			return view.widthProperty();
		}

		public final double getHeight()
		{
			return view.getHeight();
		}

		public final void setHeight(double centerY)
		{
			view.setHeight(centerY);
		}

		public final DoubleProperty centerYProperty()
		{
			return view.heightProperty();
		}

		public Shape getView()
		{
			return view;
		}
	}

	private static class FrameStats
	{
		private long						frameCount;
		private double						meanFrameInterval;									// millis
		private final ReadOnlyStringWrapper	text	= new ReadOnlyStringWrapper(this, "text",
				"Frame count: 0 Average frame interval: N/A");

		public long getFrameCount()
		{
			return frameCount;
		}

		public double getMeanFrameInterval()
		{
			return meanFrameInterval;
		}

		public void addFrame(long frameDurationNanos)
		{
			meanFrameInterval = (meanFrameInterval * frameCount + frameDurationNanos / 1_000_000.0) / (frameCount + 1);
			frameCount++;
			text.set(toString());
		}

		public String getText()
		{
			return text.get();
		}

		public ReadOnlyStringProperty textProperty()
		{
			return text.getReadOnlyProperty();
		}

		@Override
		public String toString()
		{
			return String.format("Frame count: %,d Average frame interval: %.3f milliseconds", getFrameCount(),
					getMeanFrameInterval());
		}
	}
	
	//there is nothing in this world weirder than fucking pricks
	//hello world
	// wtf is going on/

	public static void main(String[] args)
	{
		launch(args);
	}
}
