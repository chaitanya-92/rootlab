import tempfile
from pathlib import Path
import unittest
from brand_kernelsu import LOGOS, logo_xml, apply
from kernel_identity import identity
import xml.etree.ElementTree as ET

class BrandingTests(unittest.TestCase):
    def test_all_three_logos_valid_vector_xml(self):
        for logo in LOGOS:
            root = ET.fromstring(logo_xml(logo))
            self.assertEqual(root.tag, "vector")
            self.assertGreaterEqual(len(root), 2)

    def test_mock_upstream_branding(self):
        with tempfile.TemporaryDirectory() as td:
            folder = Path(td)
            gradle = folder / "manager/app/build.gradle.kts"
            gradle.parent.mkdir(parents=True)
            gradle.write_text("KSU_PACKAGE_NAME KSU_NAME")
            manifest = folder / "manager/app/src/main/AndroidManifest.xml"
            manifest.parent.mkdir(parents=True)
            manifest.write_text("<manifest />")
            kbuild = folder / "kernel/Kbuild"
            kbuild.parent.mkdir(parents=True)
            kbuild.write_text("KSU_PACKAGE_NAME")
            home = folder / "manager/app/src/main/java/me/weishu/kernelsu/ui/screen/home"
            home.mkdir(parents=True)
            for theme in ("HomeMaterial.kt", "HomeMiuix.kt"):
                (home / theme).write_text(
                    "@Composable\\nprivate fun SupportLinks(\\n"
                    "    onOpenUrl: (String) -> Unit,\\n"
                    ") {\\n    Text(\"Support Us\")\\n    Text(\"Learn KernelSU\")\\n}\\n\\n"
                    "@Composable\\nprivate fun InfoCard(\\n) {}\\n"
                )
            for logo in LOGOS:
                apply(folder, "LORD H3X", "com.lordh3x.manager", logo)
                self.assertTrue((folder/"manager/app/src/main/res/drawable/ic_launcher_foreground.xml").is_file())
                for theme in ("HomeMaterial.kt", "HomeMiuix.kt"):
                    content = (home / theme).read_text()
                    self.assertIn("Built by LORD H3X", content)
                    self.assertIn("https://t.me/Who_colki", content)
                    self.assertNotIn("Support Us", content)
                    self.assertNotIn("Learn KernelSU", content)

    def test_identity_deterministic(self):
        # Not a real cert; validates deterministic formatted output only.
        output = identity("com.lordh3x.manager", b"0test")
        self.assertIn("KSU_EXPECTED_SIZE=0x5", output)
        self.assertEqual(len(output.split("KSU_EXPECTED_HASH=")[1].strip()), 64)

    def test_invalid_package_fails(self):
        with self.assertRaises(ValueError):
            identity("bad;rm -rf", b"0test")

if __name__ == "__main__":
    unittest.main()
