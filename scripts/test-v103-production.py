"""Validate original permanently signed 10.2 -> 10.3 candidate; synthetic data only."""
import base64, hashlib, json, os, pathlib, subprocess, urllib.request, zipfile
REPO='kamranvahdati-arch/VOKANO-android'
CERT='26055f09370416e9cd61c08246b80c57367470cdb6873e282b5b6521cf097202'
PACKAGE='ir.kamranvahdati.lawoffice'
out=pathlib.Path('app/build/v103-production-proof');out.mkdir(parents=True,exist_ok=True)
def run(*args):
 r=subprocess.run(args,text=True,capture_output=True,timeout=300)
 if r.returncode: raise RuntimeError(r.stdout+r.stderr)
 return r.stdout+r.stderr
def adb(*args):return run('adb',*args)
def download(item, target):
 with target.open('wb') as f:
  for sha in item['blobs']:
   req=urllib.request.Request(f'https://api.github.com/repos/{REPO}/git/blobs/{sha}',headers={'Authorization':'Bearer '+os.environ['GITHUB_TOKEN'],'Accept':'application/vnd.github+json'})
   with urllib.request.urlopen(req,timeout=60) as r:f.write(base64.b64decode(json.load(r)['content']))
 assert target.stat().st_size==item['bytes']
 assert hashlib.sha256(target.read_bytes()).hexdigest()==item['sha256']
def test(name,phase=None,production_mode=None):
 args=['shell','am','instrument','-w','-r','-e','class',PACKAGE+'.'+name]
 if phase:args+=['-e','upgrade_phase',phase]
 if production_mode:args+=['-e','production_mode',production_mode]
 result=adb(*args,PACKAGE+'.test/'+PACKAGE+'.OfficeTestRunner')
 (out/(name+'-'+(phase or 'suite')+'.txt')).write_text(result)
 print(result,flush=True)
 if 'OK (' not in result or any(x in result for x in ['FAILURES','INSTRUMENTATION_FAILED','Process crashed']):
  (out/'failure-logcat.txt').write_text(adb('logcat','-d','-v','threadtime'))
  raise AssertionError('Instrumentation failed')
manifest=json.loads(pathlib.Path('release-validation/v103.json').read_text())
base=json.loads(pathlib.Path('release-validation/v102.json').read_text())
work=pathlib.Path('signed-v103');work.mkdir(exist_ok=True)
baseline=work/'VOKANO-10.2-release.apk'
download(next(x for x in base['files'] if x['name']==baseline.name),baseline)
archive=work/'candidate.zip';download(manifest['archive'],archive)
with zipfile.ZipFile(archive) as z:
 assert set(z.namelist())=={'VOKANO-10.3-candidate.apk','instrumentation.apk'}
 z.extractall(work)
candidate=work/'VOKANO-10.3-candidate.apk';instrumentation=work/'instrumentation.apk'
signer=pathlib.Path(os.environ['ANDROID_HOME'])/'build-tools/35.0.0/apksigner'
for i,apk in enumerate([baseline,candidate,instrumentation]):
 result=run(str(signer),'verify','--print-certs',str(apk));assert CERT in result
 (out/f'certificate-{i}.txt').write_text(result)
if os.environ.get('VOKANO_VALIDATION_MODE')=='clean':
 # A separate fresh emulator: never erase the upgrade fixture to claim a clean install.
 adb('install',str(candidate));adb('install',str(instrumentation))
 installed=adb('shell','dumpsys','package',PACKAGE);(out/'clean-package.txt').write_text(installed)
 assert 'versionCode=16 ' in installed and 'versionName=10.3' in installed
 test('ProductionBaselineTest',production_mode='seed')
 # ProductionBaselineTest asserts every exported table is empty before seeding
 # synthetic backup fixtures, checks no demo records and exercises complete backup.
 (out/'provenance.json').write_text(json.dumps(manifest,indent=2))
 print('Permanent-signed 10.3 fresh install and empty-database/full-backup checks PASS',flush=True)
 raise SystemExit(0)
# Fresh emulator installs the original production APK. No app-data clearing or uninstall.
adb('install',str(baseline));adb('install',str(instrumentation))
test('V103UpgradeTest','seed');test('V103UpgradeTest','baseline-reopen')
before=adb('shell','dumpsys','package',PACKAGE);(out/'before-package.txt').write_text(before)
assert 'versionCode=15 ' in before and 'versionName=10.2' in before
adb('shell','am','force-stop',PACKAGE);adb('install','-r',str(candidate))
test('V103UpgradeTest','verify')
for name in ['CalculationStorageTest','CalculationUiTest','V102StorageTest','WorkspaceProviderTest','WelcomeEntryTest','UiFlowSmokeTest']:
 test(name)
after=adb('shell','dumpsys','package',PACKAGE);(out/'after-package.txt').write_text(after)
assert 'versionCode=16 ' in after and 'versionName=10.3' in after
(out/'provenance.json').write_text(json.dumps(manifest,indent=2))
print('Original permanent-signed 10.2 -> 10.3 candidate upgrade PASS',flush=True)
