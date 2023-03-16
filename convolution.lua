engine.name = 'Convolution'

local rxNorns = include 'lib/rxNorns'

function enc(n,d) rxNorns.encoderO:onNext({n=n, d=d}) end
function key(n,d) rxNorns.keyO:onNext({n=n, z=z}) end

rxNorns.encoderO:subscribe(function() redraw() end)
rxNorns.keyO:subscribe(function() redraw() end)

function isCurrentFile()
  --TODO ternary
  if state.ui.file == state.params.file then
    return '*';
  else
    return '';
  end
end

function irPath(file)
  return '/home/we/dust/audio/ir/' .. file;
end

function init()
  folder = paths.home.."/dust/audio/ir"
  state = {
    params = {
      xfade = 50,
      tbd = 0,
      file = 1,
    },
    ui = {
      line = 1,
      file = 1,
    },
    files = util.scandir(folder)
  }
  engine.onFile(irPath(state.files[state.ui.file]));
  engine.xfade(0);
end

function redraw()
  screen.clear()

  screen.move(64, 10)
  screen.level(15)
  screen.text_center('Convolution Reverb')

  if state.ui.line == 1 then screen.level(15) else screen.level(1) end
  screen.move(10,30)
  screen.text("mix: ")
  screen.move(118,30)
  screen.text_right(state.params.xfade .. '%')

  if state.ui.line == 2 then screen.level(15) else screen.level(1) end
  screen.move(10,40)
  screen.text("TBD Param: ")
  screen.move(118,40)
  screen.text_right(state.params.tbd .. '%')

  if state.ui.line == 3 then screen.level(15) else screen.level(1) end
  screen.move(10,50)
  screen.text("ir: ")
  screen.move(118,50)
  screen.text_right(isCurrentFile() .. state.files[state.ui.file])

  screen.update()
end

-- mix
rxNorns.enc3
:filter(function() return state.ui.line == 1 end)
:scan(function(acc, d) return util.clamp(acc + d, 0, 100) end, 50)
:subscribe(function (d)
  state.params.xfade = d;
  engine.xfade((d - 50) / 50); --LinXFade2 wants -1 to 1
end)

-- file select
rxNorns.enc3
:filter(function() return state.ui.line == 3 end)
:scan(function(acc, d) return util.clamp(acc + d, 1, #state.files) end, 1)
:subscribe(function (d)
  state.ui.file = d;
end)

-- menu move
rxNorns.enc2
:scan(function(acc, d) return util.clamp(acc + d, 1, 3) end, 1)
:subscribe(function (d) state.ui.line = d end)

-- send IR to SC
rxNorns.key3
:filter(function() return state.ui.line == 3 end)
:subscribe(function()
  state.params.file = state.ui.file;
  engine.onFile(irPath(state.files[state.params.file]));
end)
