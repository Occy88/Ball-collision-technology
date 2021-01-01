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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

public class Blocks2 extends Application
{
	static double					eats		= 0.3;
	static double					hurts		= 0.4;
	static double					reproRate	= 0.03;
	// reduce food as eaten
	static int						reduction	=3;
	// increase in food over time
	static double					increase	= .3;
	int								counterRed	= 0, counterBlue = 0, counterGreen = 0;

	int								counter1	= 0;
	double							remx		= 0, remy = 0, remX = 0, remY = 0, remc = 1;
	boolean							remc1		= false;
	private ObservableList<Thing>	balls		= FXCollections.observableArrayList();
	private ObservableList<Area>	areas		= FXCollections.observableArrayList();
	private static final int		NUM_BALLS	= 20;
	private static final double		MIN_RADIUS	= 10;
	private static final double		MAX_RADIUS	= 20;
	private static final double		MIN_SPEED	= 20;
	private static final double		MAX_SPEED	= 20;
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
					areas.clear();
					remc1 = false;

					initiateArea(MAX_RADIUS, MIN_RADIUS, ballContainer.getWidth(), ballContainer.getHeight());
					balls.clear();
					createBalls(NUM_BALLS, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, ballContainer.getWidth(),
							ballContainer.getHeight());
				}
			}
		});

		areas.addListener(new ListChangeListener<Area>()
		{
			@Override
			public void onChanged(Change<? extends Area> change)
			{
				while (change.next())
				{
					for (Area b : change.getAddedSubList())
					{
						ballContainer.getChildren().add(b.getView());
					}
					for (Area b : change.getRemoved())
					{
						ballContainer.getChildren().remove(b.getView());
					}
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
		initiateArea(MAX_RADIUS, MIN_RADIUS, ballContainer.getHeight(), ballContainer.getWidth());
		final BorderPane root = new BorderPane();
		final Label stats = new Label();
		stats.textProperty().bind(frameStats.textProperty());

		root.setCenter(ballContainer);
		root.setBottom(stats);

		final Scene scene = new Scene(root, 1500, 1000);
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
			{// there is nothing//in this world that i hate more than being told what to do
				// ever... i fucking hate that feeling and wish it would never happen
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
		int index = 0;
		int addIndex = -1;
		LinkedList<Integer> remove = new LinkedList<Integer>();
		double[][] addit = new double[401][7];

		double elapsedSeconds = elapsedTime / 1000000000.0;
		double cx = 0, cy = 0, rd = 0, vx = 0, vy = 0, ma = 0, rd2 = 0, ma2 = 0;
		int[] colorar = new int[401];
		int color = 0;
		counter1++;
		for (Thing b1 : balls)
		{
			double x = b1.getCenterX();
			double y = b1.getCenterY();
			if (counter1 % 15 == 0)
			{

				color = b1.getColor();
				switch (color)
				{
					case 0:
						counterRed++;
						break;
					case 1:
						counterGreen++;
						break;
					case 2:
						counterBlue++;
						break;

				}
			}
			if (b1.getRadius() < MAX_RADIUS / 1.9)
			{
				remove.add(index);
			}

			while (b1.getRadius() > MAX_RADIUS * 2 && addIndex < 400)
			{

				cx = x;// Math.random()*1400+50;
				cy = y;// Math.random()*900+50;
				rd = b1.getRadius() / 2;
				vx = b1.getXVelocity();
				vy = b1.getYVelocity();
				ma = Math.pow(((b1.getRadius() / 1.5) / 40), 3);
				rd2 = b1.getRadius() / 2;
				ma2 = Math.pow(((b1.getRadius() / 1.5) / 40), 3);

				addIndex++;
				colorar[addIndex] = b1.getColor();
				addit[addIndex][0] = cx;
				addit[addIndex][1] = cy;
				addit[addIndex][2] = rd;

				addit[addIndex][5] = ma;
				addit[addIndex][6] = color;
				//
				int direct=(int) (Math.random()*2);
				switch(direct)
				{
					case 0: direct=1;break;
					case 1: direct=-1;break;
					
				}
				
				vx *= direct*.9;
				vy *= direct*-.9;
				addit[addIndex][3] = vx;
				addit[addIndex][4] = vy;

				b1.setRadius(rd2);
				b1.setMass(ma2);

			}
			index++;
			b1.setCenterX(x + elapsedSeconds * b1.getXVelocity());
			b1.setCenterY(y + elapsedSeconds * b1.getYVelocity());

		}
		Iterator<Integer> rem = remove.iterator();
		// System.out.println(addIndex);
		while (rem.hasNext())
		{
			int remo = rem.next();
			if (remo < balls.size())
			{
				balls.remove(remo);
			}

		}
		remove.clear();
		for (int x3 = 0; x3 <= addIndex; x3++)
		{

			Thing child = new Thing(addit[x3][0], addit[x3][1], addit[x3][2], addit[x3][3], addit[x3][4], addit[x3][5]);
			
			child.setColor(colorar[x3]);
			balls.add(child);
		}
//System.out.println(addIndex);
		if (counter1 % 15 == 0)
		{
			// System.out.print(" Red: "+counterRed);
			// System.out.print(" Green: "+counterGreen);
			// System.out.println(" Blue: "+counterBlue);
			System.out.println(counterRed + "	" + counterGreen + "	" + counterBlue);
			counterRed = 0;
			counterGreen = 0;
			counterBlue = 0;
		}

	}

	private static class Grid
	{

		Thing[][][] a;

		public Grid(int y, int x)
		{
			// assigning to specific location on gridd.
			a = new Thing[y + 150][x + 200][20];

		}

		public void addThing(int x, int y, Thing t)
		{

			int loc = 0;
			if (x < 0)
			{
				x = 0;
			}
			if (y < 0)
			{
				y = 0;
			}

			// System.out.println(x+","+y);
			for (int z = 0; a[y][x][z] != null && z < 10; z++)
			{
				loc++;
			}
			// System.out.println(x+",,,"+y);
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
		int gSizeX = (int) (maxX / gSize);

		int gSizeY = (int) (maxY / gSize);
		// System.out.println(gSizeX+","+gSizeY);
		Grid grid = new Grid(gSizeY, gSizeX);
		// System.out.println("new");
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
			int y2 = (int) (b1.getCenterY() + radius);// work out on paper

			// block[y1][x1]=0;

			if (x1 / gSize != x2 / gSize && y1 / gSize != y2 / gSize)// if one side
			{
				// System.out.println("\n"+y1+","+y2 );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				grid.addThing(x1 / gSize, y2 / gSize, b1);
				grid.addThing(x2 / gSize, y1 / gSize, b1);
				grid.addThing(x1 / gSize, y1 / gSize, b1);
				continue;
			} else if (x1 / gSize != x2 / gSize && y1 / gSize == y2 / gSize)
			{
				// System.out.println("\n"+y1+","+y2 );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				grid.addThing(x1 / gSize, y2 / gSize, b1);
				continue;
			} else if (x1 / gSize == x2 / gSize && y1 / gSize != y2 / gSize)
			{
				// System.out.println("\n"+y1+","+y2 );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				grid.addThing(x2 / gSize, y1 / gSize, b1);
				// System.out.println(x2/gSize);
				continue;
			} else
			{
				// System.out.println("\n"+y1+","+y2 );
				grid.addThing(x2 / gSize, y2 / gSize, b1);
				continue;
			}

		}

		ListIterator<Area> highlight = areas.listIterator();

		Area a = null;
		if (highlight.hasNext())
		{
			a = highlight.next();
		}
		// area a is the same as grid, so no need to find the location, just go down the
		// list untill you match with the grid.

		for (int y = 0; y < gSizeY + 1; y++)
		{
			for (int x = 0; x < gSizeX + 1; x++)
			{

				for (int z = 0; z < 10; z++)
				{

					if (grid.a[y][x][z] == null)
					{

						if (highlight.hasNext())
						{// refill the spot if needed before going to the next one:D
							a.increase();
							a = highlight.next();
						}
						break;
					} else if (grid.a[y][x][1] == null)
					{
						if (highlight.hasNext())
						{
							// transfer fill from background (only the specific color, educe it when called)
							// to the circle grid.a and increase it's radius by a fraction.
							//
							if (a.reduce(grid.a[y][x][z].getColor()))
							{
								a.updateColor();
								grid.a[y][x][z].increaseR();
							} else
							{
								grid.a[y][x][z].reduceR();
							}
							a = highlight.next();
						}
						break;
					} else
					{

						for (int k = z + 1; grid.a[y][x][k] != null; k++)
						{
							if (a != null)
							{
								if (a.reduce(grid.a[y][x][z].getColor()))
								{
									a.updateColor();
									grid.a[y][x][z].increaseR();
								} else
								{
									grid.a[y][x][z].reduceR();
								}
							}

							final double deltaX = grid.getThing(x, y, z).getCenterX()
									- grid.getThing(x, y, k).getCenterX();
							final double deltaY = grid.getThing(x, y, z).getCenterY()
									- grid.getThing(x, y, k).getCenterY();
							// System.out.println(grid.getThing(x, y, z).getCenterX()+", "+grid.getThing(x,
							// y, k).getCenterX());
							if (deltaY == 0 && deltaX == 0)
							{
								break;
							}

							if (colliding(grid.getThing(x, y, z), grid.getThing(x, y, k), deltaX, deltaY))
							{
								bounce(grid.getThing(x, y, z), grid.getThing(x, y, k), deltaX, deltaY);
								int color1 = grid.getThing(x, y, z).getColor(),
										color2 = grid.getThing(x, y, k).getColor();
								if (color1 == 0 && color2 == 1)
								{
									grid.getThing(x, y, k).hurt();
									grid.getThing(x, y, z).eat();
									// balls.remove(grid.getThing(x, y, k));break;
								}
								if (color1 == 1 && color2 == 0)
								{
									grid.getThing(x, y, z).hurt();
									grid.getThing(x, y, k).eat();
									// balls.remove(grid.getThing(x, y, z));break;
								}

								if (color1 == 1 && color2 == 2)
								{
									grid.getThing(x, y, k).hurt();
									grid.getThing(x, y, z).eat();
									// balls.remove(grid.getThing(x, y, k));break;
								}
								if (color1 == 2 && color2 == 1)
								{
									// balls.remove(grid.getThing(x, y, z));break;

									grid.getThing(x, y, z).hurt();
									grid.getThing(x, y, k).eat();
								}
								if (color1 == 2 && color2 == 0)
								{
									grid.getThing(x, y, k).hurt();
									grid.getThing(x, y, z).eat();
									// balls.remove(grid.getThing(x, y, k));break;
								}
								if (color1 == 0 && color2 == 2)
								{
									// balls.remove(grid.getThing(x, y, z));break;
									grid.getThing(x, y, z).hurt();
									grid.getThing(x, y, k).eat();
								}
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

		final double u1PerpX = xVelocity1 - u1 * unitContactX; // Components of ball 1 velocity in direction
																// perpendicular
		final double u1PerpY = yVelocity1 - u1 * unitContactY; // to contact vector. This doesn't change with collision
		final double u2PerpX = xVelocity2 - u2 * unitContactX; // Same for ball 2....
		final double u2PerpY = yVelocity2 - u2 * unitContactY;

		b1.setXVelocity(v1 * unitContactX + u1PerpX);
		b1.setYVelocity(v1 * unitContactY + u1PerpY);
		b2.setXVelocity(v2 * unitContactX + u2PerpX);
		b2.setYVelocity(v2 * unitContactY + u2PerpY);

	}

	private void initiateArea(double maxRadius, double minRadius, double maxX, double maxY)
	{
		int gSize = (int) (1.5 * (maxRadius + minRadius));

		int gSizeX = (int) (maxX / (gSize));
		int gSizeY = (int) (maxY / (gSize));
		// System.out.println(gSizeX+","+gSizeY);
		int red = 0, green = 0, blue = 0;

		for (int y = 0; y < gSizeY + 1; y++)
		{
			for (int x = 0; x < gSizeX + 1; x++)
			{
				int random1 = (int) (Math.random() * 2);
				switch (random1)
				{
					case 0:
						red -= 40;
						if (red <= 30)
						{
							red += 40;
						}
						;
						break;
					case 1:
						red += 40;
						if (red >= 225)
						{
							red -= 40;
						}
						;
						break;

				}
				random1 = (int) (Math.random() * 2);
				switch (random1)
				{
					case 0:
						green -= 40;
						if (green <= 30)
						{
							green += 40;
						}
						;
						break;
					case 1:
						green += 40;
						if (green >= 225)
						{
							green -= 40;
						}
						;
						break;

				}
				random1 = (int) (Math.random() * 2);
				switch (random1)
				{
					case 0:
						blue -= 40;
						if (blue <= 30)
						{
							blue += 40;
						}
						;
						break;
					case 1:
						blue += 40;
						if (blue >= 225)
						{
							blue -= 40;
						}
						;
						break;

				}
				double locationX = x * gSize;
				double locationY = y * gSize;
				Area area = new Area(locationY, locationX, gSize, 255, 255, 255);
				areas.add(area);
			}
		}
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
			final double angle = Math.random() * 360;
			Thing ball = new Thing(Math.random()*initialX*0.95+initialX / 98,
					Math.random() * initialY*0.95+ initialY /98, radius, speed * cos(angle), speed * sin(angle),
					mass);
			int colour = (int) (Math.random() * 4);
			//System.out.println(colour);
			switch (colour)
			{

				case 0:
					ball.setColor(0);
					break;
				case 1:
					ball.setColor(1);
					break;

				case 2:
					ball.setColor(2);
					break;
				case 3:
					ball.setColor(0);
					break;
			}

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
		private double						mass;		// arbitrary units
		private double						radius;		// pixels
		private int							color;

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

		public void setRadius(double rd2)
		{
			view.setRadius(radius = rd2);

		}

		public void setMass(double ma2)
		{
			this.mass = ma2;

		}

		public void increaseR()
		{
			view.setRadius(radius += (reproRate - getSpeed() / 20000));
			this.mass = Math.pow((radius / 40), 3);
		}

		public void eat()
		{
			view.setRadius(radius *= eats + 1);
			this.mass = Math.pow((radius / 40), 3);
		}

		public void hurt()
		{
			view.setRadius(radius *= hurts);
			this.mass = Math.pow((radius / 40), 3);

		}

		public void reduceR()
		{
			view.setRadius(radius -= (0.02 + getSpeed() / 20000));
		}

		public final void setColor(int color)
		{
			this.color = color;
		//	view.setFill(null);
			switch (color)
			{
				case 0:
					view.setFill(Color.rgb(255, 0, 0));

					break;
				case 1:
					view.setFill(Color.rgb(0, 255, 0));
					break;

				case 2:
					view.setFill(Color.rgb(0, 0, 255));
					break;

			}

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

		public int getColor()
		{
			return color;
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

		private final Rectangle	view;
		private int				brightness	= 0;
		private double			red			= 0;
		private double			blue		= 0;
		private double			green		= 0;

		public Area(double height, double width, double multiple, int red, int green, int blue)
		{
			this.view = new Rectangle(multiple, multiple);
			// only happens when objects are initiated.
			//System.out.println((int) Math.random() * 256);
			view.setFill(Color.rgb(red, green, blue, 0.5));
			this.red = red;
			this.blue = blue;
			this.green = green;

			view.setX(width);
			view.setY(height);
		}

		public boolean reduce(int color)
		{
			boolean rtr = true;

			switch (color)
			{
				case 0:
					if (this.green < 240)
					{
						this.green += increase*2;
					}
					if (this.blue < 240)
					{
						this.blue += increase*2;
					}
					if (this.red > 10)
					{
						this.red -= reduction;

						return rtr = this.red > 0 ? true : false;
					} else
						break;
				case 1:
					if (this.blue < 240)
					{
						this.blue += increase*2;
					}
					if (this.red < 240)
					{
						this.red += increase*2;
					}
					if (this.green > 10)
					{
						this.green -= reduction;

						return rtr = this.green > 0 ? true : false;
					} else
						break;
				case 2:
					if (this.green < 240)
					{
						this.green += increase*2;
					}
					if (this.red < 240)
					{
						this.red += increase*2;
					}
					if (this.blue > 10)
					{
						this.blue -= reduction;

						return rtr = this.blue > 0 ? true : false;
					} else
						break;
			}
			return false;

		}

		public void increase()
		{
			if (this.red < 240)
			{
				this.red += increase;
			}
			if (this.blue < 240)
			{
				this.blue += increase;
			}
			if (this.green < 240)
			{
				this.green += increase;
			}
			updateColor();
		}

		public void updateColor()
		{
			view.setFill(Color.rgb((int) red, (int) blue, (int) green));
		}

		public void setBlue(int blue)
		{
			view.setFill(Color.rgb((int) red, blue, (int) green));
			this.blue = blue;
		}

		public void setGreen(int green)
		{
			view.setFill(Color.rgb((int) red, (int) blue, green));
			this.green = green;
		}

		public void setRed(int red)
		{
			view.setFill(Color.rgb(red, (int) blue, (int) green));
			this.red = red;
		}

		public int getBlue()
		{
			return (int) blue;
		}
		// return colours

		public int getGreen()
		{
			return (int) green;
		}

		public int getRed()
		{
			return (int) red;
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

	

	public static void main(String[] args)
	{
		launch(args);
	}
}
