local rx = require('rx')
local rxNorns = include 'lib/rxNorns'

function enc(n,d) rxNorns.encoderO:onNext({n=n, d=d}) end

rxNorns.encoderO:subscribe(function() redraw() end)
local function createEncObs(n) return rxNorns.encoderO:filter(function(a) return a.n == n end):pluck('d') end
local enc2 = createEncObs(2)
local enc3 = createEncObs(3)

local SMOOTH_SIZE = 5
local FONT_SIZE = 10
function init()
	folder = paths.home.."/dust/audio/ir"
	state = {
		params = {
			50,
			0,
			1,
		},
		ui = {
			line = 1
		},
		files = util.scandir(folder)
	}
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
	screen.text_right(state.params[1] .. '%')

	if state.ui.line == 2 then screen.level(15) else screen.level(1) end
	screen.move(10,40)
	screen.text("decay: ")
	screen.move(118,40)
	screen.text_right(state.params[2] .. '%')

	if state.ui.line == 3 then screen.level(15) else screen.level(1) end
	screen.move(10,50)
	screen.text("ir: ")
	screen.move(118,50)
	screen.text_right(state.files[state.params[3]])

	screen.update()
end

enc3
:filter(function() return state.ui.line ~= 3 end)
:scan(function(acc, d) return util.clamp(acc + d, 0, 100) end, 50)
:subscribe(function (d) state.params[state.ui.line] = d end)

enc3
:filter(function() return state.ui.line == 3 end)
:scan(function(acc, d) return util.clamp(acc + d, 1, #state.files) end, 1)
:subscribe(function (d) state.params[3] = d end)

enc2
:scan(function(acc, d) return util.clamp(acc + d, 1, 3) end, 50)
:subscribe(function (d) state.ui.line = d end)
