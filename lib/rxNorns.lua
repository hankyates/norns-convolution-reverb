-- TODO package rx with script
local rx = require('rx')

local encoderO = rx.Subject.create()
local keyO = rx.Subject.create()

-- add to script file
-- function enc(n,d) rxNorns.encoderO:onNext({n=n, d=d}) end
local function createEncObs(n) return encoderO:filter(function(a) return a.n == n end):pluck('d') end
local enc1 = createEncObs(1)
local enc2 = createEncObs(2)
local enc3 = createEncObs(3)

-- add to script file
-- function key(n,d) rxNorns.keyO:onNext({n=n, z=z}) end
local function createKeyObs(n) return keyO:filter(function(a) return a.n == n end):pluck('z') end
local key1 = createKeyObs(1)
local key2 = createKeyObs(2)
local key3 = createKeyObs(3)

return {
  encoderO = encoderO,
  enc1 = enc1,
  enc2 = enc2,
  enc3 = enc3,
  keyO = keyO,
  key1 = key1,
  key2 = key2,
  key3 = key3
}
