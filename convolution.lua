engine.name = 'Convolution'

local rxNorns = include 'lib/rxNorns'

function enc(n,d) rxNorns.encoderO:onNext({n=n, d=d}) end
--function key(n,d) rxNorns.keyO:onNext({n=n, z=z}) end

rxNorns.encoderO:subscribe(function() redraw() end)
--rxNorns.keyO:subscribe(function() redraw() end)

function scale(n)
  return n * 0.01;
end

function init()
  state = {
    params = {
      dry = 100,
      wet = 0,
    },
    ui = {
      line = 1,
    },
  }
  engine.dry(scale(state.params.dry));
  engine.wet(scale(state.params.wet));
end

function redraw()
  screen.clear()

  screen.move(64, 10)
  screen.level(15)
  screen.text_center('Convolution Reverb')

  if state.ui.line == 1 then screen.level(15) else screen.level(1) end
  screen.move(10,30)
  screen.text("dry: ")
  screen.move(118,30)
  screen.text_right(state.params.dry .. '%')

  if state.ui.line == 2 then screen.level(15) else screen.level(1) end
  screen.move(10,40)
  screen.text("wet: ")
  screen.move(118,40)
  screen.text_right(state.params.wet .. '%')

  screen.update()
end

-- menu move
rxNorns.enc2
:scan(function(acc, d) return util.clamp(acc + d, 1, 2) end, 1)
:subscribe(function (d) state.ui.line = d end)

-- dry
rxNorns.enc3
:filter(function() return state.ui.line == 1 end)
:scan(function(acc, d) return util.clamp(acc + d, 0, 100) end)
:subscribe(function (d)
  state.params.dry = d or state.params.dry;
  engine.dry(scale(d));
end)

-- wet
rxNorns.enc3
:filter(function() return state.ui.line == 2 end)
:scan(function(acc, d) return util.clamp(acc + d, 0, 100) end)
:subscribe(function (d)
  state.params.wet = d or state.params.wet;
  engine.wet(scale(d));
end)
