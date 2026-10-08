import fs from 'fs';
import path from 'path';

const root = path.join(__dirname, '..', '..');
const manifest = fs.readFileSync(path.join(root, 'Package.swift'), 'utf8');
const pkg = JSON.parse(
  fs.readFileSync(path.join(root, 'package.json'), 'utf8')
);
const rnConfig = require(path.join(root, 'react-native.config.js'));

const filesIn = (dir: string, ext: string) =>
  fs
    .readdirSync(path.join(root, dir), { recursive: true, encoding: 'utf8' })
    .filter((file) => file.endsWith(ext));

// SwiftPM can't mix Swift and Objective-C in one target, so Package.swift splits ios/ by
// directory. CocoaPods globs ios/**, so a file in the wrong place builds there and breaks
// only in SwiftPM mode, which CI doesn't build.
describe('Package.swift', () => {
  it('keeps every Swift source under ios/Swift', () => {
    expect(manifest).toContain('path: "ios/Swift"');
    expect(filesIn('ios', '.swift').every((f) => f.startsWith('Swift/'))).toBe(
      true
    );
  });

  it('keeps Objective-C sources out of ios/Swift', () => {
    expect(manifest).toContain('exclude: ["Swift"]');
    const objc = [...filesIn('ios', '.mm'), ...filesIn('ios', '.m')];
    expect(objc.length).toBeGreaterThan(0);
    expect(objc.some((f) => f.startsWith('Swift/'))).toBe(false);
  });

  // The .mm files register Swift classes by Objective-C name from another SwiftPM target.
  // Release builds give internal classes a local symbol, so the link fails unless they're public.
  it('makes every Swift class registered from Objective-C public', () => {
    const read = (file: string) =>
      fs.readFileSync(path.join(root, 'ios', file), 'utf8');
    const registered = filesIn('ios', '.mm').flatMap((f) =>
      [...read(f).matchAll(/RCT_EXTERN_MODULE\((\w+),/g)].map((m) => m[1])
    );
    const swift = filesIn('ios', '.swift').map(read).join('\n');
    expect(registered.length).toBeGreaterThan(0);
    for (const name of registered) {
      expect(swift).toMatch(
        new RegExp(`@objc\\(${name}\\)\\s*\\n\\s*public class `)
      );
    }
  });

  it('uses the same name everywhere React Native looks it up', () => {
    const name = 'ReactNativePortals';
    expect(manifest).toContain(`name: "${name}"`);
    expect(manifest).toContain(`.library(name: "${name}"`);
    expect(rnConfig.spm.name).toBe(name);
    expect(pkg.swiftpmConfig.name).toBe(name);
  });

  it('ships in the npm package', () => {
    expect(pkg.files).toEqual(
      expect.arrayContaining(['Package.swift', 'react-native.config.js'])
    );
  });

  it('matches the IonicPortals version the podspec requires', () => {
    const podspec = fs.readFileSync(
      path.join(root, 'ReactNativePortals.podspec'),
      'utf8'
    );
    const podVersion = /'IonicPortals', '~> (\d+\.\d+)\.\d+'/.exec(
      podspec
    )?.[1];
    expect(podVersion).toBeDefined();
    expect(manifest).toMatch(
      new RegExp(`ionic-portals-ios", "${podVersion}\\.\\d+"\\.\\.<`)
    );
  });
});
