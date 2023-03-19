Engine_Convolution : CroneEngine {
  var br, bl, bufsize, bsr, bsl, irBuffer, convolution,
  n, d, e, response, onepole, nchannels = 2;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {

    ~fftsize = 1024;

    // Taken from:
    // https://github.com/alikthename/Musical-Design-in-Supercollider/blob/master/9_reverb_sound_image_of_space-convolution.sc#L34
    n = 2 * 48000;
    // white noise
    //d = nchannels.collect{ n.collect{ |j| var p = j/n; [ 0, rrand(-0.5,0.5)].wchoose([ 1 - p, p ])} };
    // ~gaussian~ noise
    //d = nchannels.collect{ n.collect{ |j| var p = j/n; [ 0, sum3rand(0.5)].wchoose([ 1 - p, p ])} };
    /// velvet noise
    d = nchannels.collect{ n.collect{ |j| var p = j/n; [ 0, [-0.5,0.5].choose].wchoose([ 1 - p, p ])} };
    onepole = {arg input, startcoef=0.5, absorpCurve = 0.4;
      var coef = startcoef, coef_;
      var outPrev = input[0];
      (input.size-1).collect({|i|
        coef = coef + (input.size.reciprocal * (1 - startcoef ));
        coef_ = coef.pow(absorpCurve);
        outPrev = ((1 - coef_) * input[i+1]) + (coef_ * outPrev);
        outPrev;
      })
    };
    d = d.collect({|it i| onepole.value(it, 0.7, 0.8) });
    e = Env([ 1, 1, 0 ], [ 0.1,1.9 ], -6).discretize(n);
    response = d.collect({|it| it * e });
    irBuffer = nchannels.collect { |i| Buffer.loadCollection(context.server, response[i]) };

    // Swap this with code above.
    //irBuffer = Buffer.read(context.server, "/home/we/dust/audio/ir/ir-1-18dbfstp.wav");

    context.server.sync;

    bufsize = PartConv.calcBufSize(~fftsize, irBuffer[0]);

    ~irBufL = Buffer.alloc(context.server, bufsize, 1);
    ~irBufL.preparePartConv(irBuffer[0], ~fftsize);

    ~irBufR = Buffer.alloc(context.server, bufsize, 1);
    ~irBufR.preparePartConv(irBuffer[1], ~fftsize);

    context.server.sync;

    irBuffer.free;

    SynthDef(\conv, {
      arg dry, wet;

      var sigL = SoundIn.ar(0);
      var sigR = SoundIn.ar(1);

      var verbL = PartConv.ar(sigL, ~fftsize, ~irBufL.bufnum);
      var verbR = PartConv.ar(sigL, ~fftsize, ~irBufR.bufnum);

      // crossfeed
      //var outL = LinXFade2.ar(sigL, verbL, xfade);
      //var outR = LinXFade2.ar(sigR, verbR, xfade);

      var outL = Mix([sigL * dry, verbL * wet * 0.2]);
      var outR = Mix([sigR * dry, verbR * wet * 0.2]);

      Out.ar(0, [outL, outR]);

    }).add;

    convolution = Synth.new(\conv, [
      \dry, 0,
      \wet, 0
    ], target: context.xg);

    this.addCommand("dry", "f", { arg msg;
      convolution.set(\dry, msg[1]);
    });

    this.addCommand("wet", "f", { arg msg;
      convolution.set(\wet, msg[1]);
    });

  }
}
