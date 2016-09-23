//MY DUMB PROJECT FROM GRADE 11 REWRITTEN IN JAVASCRIPT WITH NO CONCERN FOR
//MAINTANABILITY WHICH IS FUNNY BECAUSE THE ONLY REASON THIS IS POSSIBLE IS
//BECAUSE THE ORIGINAL JAVA IS PRETTY WELL ORGANIZED/COMMENTED.


var canvas = document.getElementById("exciteCanvas");
var ctx = canvas.getContext("2d");

bigFont = "48px Pixelfont";
titleFont = "148px Pixelfont";
smallFont = "30px Pixelfont";

var menuMusic = new Audio("sounds/pixel-peeker-polka.wav");
var explosion = new Audio("sounds/explosion.wav");
var coin = new Audio("sounds/coin.wav");

var musicPlaying = false;

var randomColor = getRandomColor();
var colCounter = 0;
var frameCounter = 0;

var state = "menu";
var overlayOpacity = 0;

var collidex = 0;
var collidey = 0;

var keysPressed = {
  space: false,
  escape: false,
};


var blocks = [];
for (i = 0; i < 128; i++) {
  blocks[i] = [];
  for (j = 0; j < 72; j++) {
    blocks[i][j] = { x: i*10, y: j*10,
                     type: "empty",
                     dimness: 0,
                     powerup: false,
                     color: "#000000",
                     deadly: false
                   };
  }
}

function clearBlock(b) {
  b.type = "empty";
  b.dimness = 0;
  b.powerup = false;
  b.color = "#000000";
  deadly = false;
}

function Player(playerNum) {
  switch (playerNum) {
    case 1:
      this.x = 980;
      this.y = 360;
      this.direction = "west";
      this.color = "#FF0000";
      this.type = "player1";
      break;
    case 2:
      this.direction = "east";
      this.x = 300;
      this.y = 360;
      this.color = "#FF8C00";
      this.type = "player2";
      break;
  }

  this.toChange = false;
  this.dist = 3;
  this.lost = false;
  this.winCount = 0;
  this.score = 0;
  this.powerup = "none";
  this.timer = 0;
  this.moveTimer = 0;

  this.onGrid = function() {
    return this.x % 10 === 0 && this.y % 10 === 0;
  };
  this.go = function(direction) {
    switch (direction) {
      case "north":
        if (this.direction !== "south") {
          this.toChange = true;
          this.changeTo = "north";
        }
        break;
      case "west":
        if (this.direction !== "east") {
          this.toChange = true;
          this.changeTo = "west";
        }
        break;
      case "south":
        if (this.direction !== "north") {
          this.toChange = true;
          this.changeTo = "south";
        }
        break;
      case "east":
        if (this.direction !== "west") {
          this.toChange = true;
          this.changeTo = "east";
        }
        break;
    }

  };
  this.changeDirection = function() {
    if (this.toChange && this.onGrid()) {
      this.direction = this.changeTo;
      this.toChange = false;
    }
  };
  this.move = function() {
    if (this.moveTimer % 3 === 0 && this.dist === 3) {
      this.dist = 4;
    }
    else if (this.dist === 4) {
      this.dist = 3;
    }
    this.moveTimer += 1;

    if (this.onGrid()) {
      if (this.powerup === "speed" || this.powerup === "breaker") {
        this.timer += 1;
        if (this.timer >= 480) {
          this.dist = 3;
          this.moveTimer = 0;
          this.powerup = "none";
          if (this.type === "player1") {
            this.color = "#FF0000";
          }
          else {
            this.color = "#FF8C00";
          }
        }
      }
    }

    this.changeDirection();

    switch (this.direction) {
      case "west":
        this.x -= this.dist;
        break;
      case "east":
        this.x += this.dist;
        break;
      case "north":
        this.y -= this.dist;
        break;
      case "south":
        this.y += this.dist;
        break;
    }

    if (this.onGrid()) {
      var b = blocks[this.x/10][this.y/10];

      var moved = false;

      if (this.direction === "north" || this.direction === "south") {
        if (this.y >= 700) {
          this.createTail(b);
          this.y = 10;
          moved = true;
        }
        else if (this.y <= 10) {
          this.createTail(b);
          this.y = 700;
          moved = true;
        }
      }
      if (this.direction === "east" || this.direction === "west") {
        if (this.x >= 1260) {
          this.createTail(b);
          this.x = 10;
          moved = true;
        }
        else if (this.x <= 10) {
          this.createTail(b);
          this.x = 1260;
          moved = true;
        }
      }


      if (moved) {
        //console.log(this.x, this.y);
        b = blocks[this.x/10][this.y/10];
      }

      if (b.type !== "empty") {
        if (!b.powerup) {
          if (b.deadly && this.powerup !== "breaker") {
            this.lost = true;


          }
          else if (b.deadly && this.powerup === "breaker") {
            switch (this.direction) {
              case "west":
                clearBlock(blocks[this.x/10-1][this.y/10-1]);
                clearBlock(blocks[this.x/10][this.y/10-1]);
                //clearBlock(blocks[this.x/10-1][this.y/10]);
                clearBlock(blocks[this.x/10-1][this.y/10+1]);
                clearBlock(blocks[this.x/10][this.y/10+1]);
                break;
              case "east":
                clearBlock(blocks[this.x/10][this.y/10-1]);
                clearBlock(blocks[this.x/10+1][this.y/10-1]);
                //clearBlock(blocks[this.x/10+1][this.y/10]);
                clearBlock(blocks[this.x/10][this.y/10+1]);
                clearBlock(blocks[this.x/10+1][this.y/10+1]);
                break;
              case "north":
                clearBlock(blocks[this.x/10-1][this.y/10-1]);
                //clearBlock(blocks[this.x/10][this.y/10-1]);
                clearBlock(blocks[this.x/10+1][this.y/10-1]);
                clearBlock(blocks[this.x/10-1][this.y/10]);
                clearBlock(blocks[this.x/10+1][this.y/10]);
                break;
              case "south":
                clearBlock(blocks[this.x/10-1][this.y/10]);
                clearBlock(blocks[this.x/10+1][this.y/10]);
                clearBlock(blocks[this.x/10-1][this.y/10+1]);
                //clearBlock(blocks[this.x/10][this.y/10+1]);
                clearBlock(blocks[this.x/10+1][this.y/10+1]);
                break;
            }
            this.createTail(b);
          }
        }
        else {
          b.powerup = false;
          switch (b.type) {
          case "money":
            this.score += 1;
            break;
          case "speed":
            this.powerup = "speed";
            this.dist = 5;
            this.timer = 0;
            this.color = b.color;
            break;
          case "star":
            break;
          case "break":
            this.powerup = "breaker";
            this.color = b.color;
            this.timer = 0;
            break;
          }
          this.createTail(b);
        }
      }
      else {
        this.createTail(b);
      }
    }

  };
  this.drawPlayer = function() {
    ctx.beginPath();
    ctx.rect(this.x, this.y, 10, 10);
    ctx.fillStyle = this.color;
    ctx.fill();
    ctx.closePath();
  };

  this.winning = function() {
    this.winCount += 1;
    this.score += 10;
  };
  this.createTail = function(b) {
    b.type = this.type;
    b.color = this.color;
    b.dimness = 0;
    b.timer = 0;
  };
}






var player1 = new Player(1);
var player2 = new Player(2);



function getRandomColor() {
  function c() {
    var hex = Math.floor(Math.random()*256).toString(16);
    return ("0"+String(hex)).substr(-2); // pad with zero
  }
  return "#"+c()+c()+c();
}

function keyDownHandler(e) {
  switch(e.keyCode) {
    case 32:
      keysPressed.space = true;
      break;
    case 27:
      keysPressed.escape = true;
      break;
    case 87:
      player2.go("north");
      break;
    case 65:
      player2.go("west");
      break;
    case 83:
      player2.go("south");
      break;
    case 68:
      player2.go("east");
      break;
    case 38:
      player1.go("north");
      break;
    case 37:
      player1.go("west");
      break;
    case 40:
      player1.go("south");
      break;
    case 39:
      player1.go("east");
      break;
  }
}
function keyUpHandler(e) {
  if (e.keyCode === 32) {
    keysPressed.space = false;
  }
  if (e.keyCode === 27) {
    keysPressed.escape = false;
  }
}

function drawMenu() {
  if (!musicPlaying) {
    menuMusic.currentTime = 0;
    menuMusic.play();
    musicPlaying = true;
  }

  ctx.font = titleFont;
  ctx.fillStyle = randomColor;
  ctx.fillText("EXCITECYCLE", 168, 345);
  ctx.fillStyle = "#FFFFFF";
  ctx.fillText("EXCITECYCLE", 163,340);

  ctx.font = smallFont;
  ctx.fillText("The 2 player fun-machine!", 163, 380);
  ctx.fillText("Player 1 [W] [A] [S] [D]", 163, 460);
  ctx.fillText("Player 2 [UP] [LEFT] [DOWN] [RIGHT]", 163, 490);
  ctx.fillText("(C) 1983 Generic Game Company, LLC", 360, 60);

  ctx.font = bigFont;
  ctx.fillStyle = randomColor;
  ctx.fillText("press space to start", 354, 680);

  if (keysPressed.space) {
    reset();
    state = "play";
    menuMusic.pause();
    musicPlaying = false;
    frameCounter = 0;
  }
}

function dim(color) {
    var r = ("0" + (Math.max(parseInt(color.substring(1,3), 16) - 0x14, 0).toString(16))).slice(-2);
    var g = ("0" + (Math.max(parseInt(color.substring(3,5), 16) - 0x14, 0).toString(16))).slice(-2);
    var b = ("0" + (Math.max(parseInt(color.substring(5,7), 16) - 0x14, 0).toString(16))).slice(-2);
    return("#"+r+g+b).toUpperCase();
}

function drawBlocks() {
  if (state !== "gameover") {
    generatePowerups();
  }

  for (i = 0; i < 128; i++) {
    for (j = 0; j < 72; j++) {
      var b = blocks[i][j];
      if (b.type !== "empty") {
        if (b.powerup && state !== "gameover") {
          b.timer += 1;

          if (b.timer === 900) {
            clearBlock(b);
            //console.log(b.timer);
         }
        }
        else if (b.dimness <= 5  && state != "gameover") {
          b.timer += 1;

          if (b.timer % 4 === 0) {
            b.deadly = true;
            b.color = dim(b.color);
            b.dimness += 1;
          }
        }

        ctx.beginPath();
        ctx.rect(b.x, b.y, 10, 10);
        ctx.fillStyle = b.color;
        ctx.fill();
        ctx.closePath();
      }
    }
  }
}


function drawGame() {
  var reset = false;
  frameCounter++;

  player1.move();
  player2.move();

  drawBlocks();
  player1.drawPlayer();
  player2.drawPlayer();

  if (player1.lost) {
    player2.winning();
    collidex = player1.x;
    collidey = player1.y;
    reset = true;
  }
  if (player2.lost) {
    player1.winning();
    collidex = player2.x;
    collidey = player2.y;
    reset = true;
  }

  drawUI();

  if (reset) {
    gameOver();
  }

  if (keysPressed.escape) {
    state = "menu";
  }
}

function gameOver() {
  overlayOpacity = 1;
  state = "gameover";
  explosion.play();
}

function drawGameOver() {
  drawBlocks();
  player1.drawPlayer();
  player2.drawPlayer();

  drawUI();

  ctx.font = bigFont;
  ctx.fillStyle = randomColor;
  ctx.fillText("press space to continue", 330, 680);

  ctx.font = smallFont;
  ctx.fillStyle = "#000000";
  ctx.fillText("ouch.", collidex-28, collidey+15);
  ctx.fillStyle = "#FFFFFF";
  ctx.fillText("ouch.", collidex-30, collidey+13);

  if (overlayOpacity > 0) {
    ctx.beginPath();
    ctx.rect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "rgba(255,255,255,"+overlayOpacity+")";
    ctx.fill();
    ctx.closePath();

    overlayOpacity -= 0.078431373;
  }

  if (keysPressed.space) {
    reset();
    state = "play";
  }
  if (keysPressed.escape) {
    state = "menu";
  }
}

function reset() {
  score1 = player1.score;
  win1 = player1.winCount;
  score2 = player2.score;
  win2 = player2.winCount;

  player1 = new Player(1);
  player1.score = score1;
  player1.winCount = win1;
  player2 = new Player(2);
  player2.score = score2;
  player2.winCount = win2;

  for (i = 0; i < 128; i++) {
    for (j = 0; j < 72; j++) {
      var b = blocks[i][j];
      clearBlock(b);
    }
  }
}

function generatePowerups() {
  var chance = Math.floor(300*Math.random());

  if (chance === 42) {
    var x = Math.floor(127*Math.random() + 1);
    var y = Math.floor(71*Math.random() + 1);

    while (blocks[x][y].color !== "#000000") {
      x = Math.floor(127*Math.random() + 1);
      y = Math.floor(71*Math.random() + 1);
    }

    var options = ["money","money","money","money","money","money","speed",
                  "speed","speed","break","break","star"];

    var type = options[Math.floor(12*Math.random())];

    var b = blocks[x][y];
    b.type = type;
    b.powerup = true;

    switch (type) {
      case "money":
        b.color = "#FFFF00";
        break;
      case "speed":
        b.color = "#00FF00";
        break;
      case "break":
        b.color = "#00FFFF";
        break;
    }
    b.timer = 0;
  }
}

function drawUI() {
  ctx.font = smallFont;
  ctx.fillStyle = player1.color;
  ctx.fillText("SCORE: " + player1.score, 1080, 60);

  ctx.fillStyle = player2.color;
  ctx.fillText("SCORE: " + player2.score, 60, 60);

  ctx.fillStyle = "#FFFFFF";
  ctx.fillText("Won: " + player1.winCount, 1080, 90);
  ctx.fillText("Won: " + player2.winCount, 60, 90);
}

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  switch (state) {
    case "menu":
      drawMenu();
      break;
    case "play":
      drawGame();
      break;
    case "gameover":
      drawGameOver();
      break;
  }

  colCounter += 1;
  if (colCounter === 10) {
    randomColor = getRandomColor();
    colCounter = 0;
  }

  requestAnimationFrame(draw);
}

document.addEventListener("keydown", keyDownHandler, false);
document.addEventListener("keyup", keyUpHandler, false);

draw();
